package portal.controllers;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.CloneCommand;

import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.Commit;
import java.io.File;
import org.apache.commons.io.FileUtils;

import java.util.*;

//import org.springframework.test.context.ContextConfiguration;
import portal.services.projects.ProjectsService;
import portal.services.projects.Project;
import portal.services.projects.CheckResult;

import java.nio.file.Files;
import java.nio.file.Paths;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
//@ContextConfiguration(classes={RESTConfiguration.class})

@Controller
public class ProjectsController {

    @Autowired
    private ProjectsService projectsService;
    //private ProjectsService projectsService = new ProjectsService();
    /*@Autowired
    private AuthService authService;*/

    Logger log = LoggerFactory.getLogger(ProjectsController.class);
    private Object object;
    String defaultPath = System.getProperty("user.dir") + File.separator + "projects" + File.separator;

    class ElementHierarchy {
        public String name;
        public String parent;
        public int level;
        public int prev_level;
        public String extension;
        public boolean isFile;
        public int countChilds;
        public ElementHierarchy(String name, String parent, int level, int prev_level, String extension, boolean isFile, int countChilds)
        {
            this.name = name;
            this.parent = parent;
            this.level = level;
            this.prev_level = prev_level;
            this.extension = extension;
            this.isFile = isFile;
            this.countChilds = countChilds;
        }
    };

    class getFileResult {
        private String message;
        private Boolean success;
        private String content;

        public getFileResult(String message, Boolean success, String content) {
            this.message = message;
            this.success = success;
            this.content = content;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) { this.message = message; }

        public String getContent() {
            return content;
        }

        public void setContent(String content) { this.content = content; }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }
    }

    public static class Wrapper {

        String file;
        String content;

        @Override
        public String toString() {
            return "Wrapper [file=" + file + ", content=" + content + "]";
        }

    }

    @RequestMapping(value = "/projects", method = RequestMethod.GET)
    public ModelAndView/*String*/ addProject(Model model) {
        /*testSomeMethod();
        log.debug("In ProjectsController... Add project ...");
        return "addProject";*/
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("addProject");
        return modelAndView;
    }

    @RequestMapping(value = "/project", method = RequestMethod.GET)
    public String viewProject(@RequestParam(value = "project_id", required = true) int project_id, Model model) {
        log.debug("In ProjectsController... View project ...");
        if (project_id > 0) {
            Project project = projectsService.selectProject(project_id);
            model.addAttribute("project", project);
            model.addAttribute("project_id", project.getId());
            File dir = new File(defaultPath + project.getPath());
            ArrayList<ElementHierarchy> hierarchy = new ArrayList<ElementHierarchy>();
            String gitDir = dir.getAbsolutePath() + File.separator + ".git";
            list(dir, gitDir, hierarchy, 0, "");
            /*for (ElementHierarchy elem: hierarchy) {
                System.out.println("Some elem: " + elem.name + " " + elem.parent + " " + elem.level + " " + elem.extension + " " + elem.isFile + "\n");
                System.out.println("Prev level: " + elem.prev_level + "\n");
            }*/
            model.addAttribute("hierarchy", hierarchy);
        }
        else {
            model.addAttribute("project_id", 0);
        }
        model.addAttribute("file_separator", File.separator);
        return "viewProject";
    }

    static int prev_level;
    private void list(File file, String gitDir, ArrayList<ElementHierarchy> hierarchy, int level, String parent) {
        if (file.getAbsolutePath().equals(gitDir)) {
            return;
        }
        if (level == 0) {
            prev_level = -1;
        }
        File[] children = file.listFiles();
        if (level > 0) {
            hierarchy.add(new ElementHierarchy(file.getName(), parent, level, prev_level, FilenameUtils.getExtension(file.getName()), file.isFile(), (children != null) ? children.length : 0));
            prev_level = level;
        }
        if (children != null) {
            if (level > 0) {
                parent = parent + File.separator + file.getName();
            }
            level++;
            for (File child : children) {
                list(child, gitDir, hierarchy, level, parent);
            }
        }
    }

    private String explodeFilesC(File file, String gitDir, String parent, int level) {
        String result = "";
        if (file.getAbsolutePath().equals(gitDir)) {
            return result;
        }
        File[] children = file.listFiles();
        if (file.isFile() && FilenameUtils.getExtension(file.getName()).equals("c")) {
            if (level > 0 && !parent.equals("")) {
                result += " " + parent + "/" + file.getName();
            }
            else {
                result += " " + file.getName();
            }
        }
        if (children != null) {
            if (level > 0) {
                if (!parent.equals("")) {
                    parent = parent + "/" + file.getName();
                }
                else {
                    parent = file.getName();
                }
            }
            level++;
            for (File child : children) {
                result += explodeFilesC(child, gitDir, parent, level);
            }
        }
        return result;
    }

    /*@RequestMapping(value = "/runcheck",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> getSearchResultViaAjax(@RequestParam//@RequestBody String some) {
        System.out.println("Some: " + some);
        return new ResponseEntity<>(new CheckResult("Result", true), HttpStatus.OK);
    }*/

    @RequestMapping(value = "/getFile",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> getFile(@RequestParam String file, @RequestParam long project_id)
    {
        String content = "";
        boolean success = true;
        String message = "";
        Project project;
        if (project_id > 0) {
            project = projectsService.selectProject(project_id);
            project_id = project.getId();
            if (project_id > 0) {
                try {
                    System.out.println("File path: " + defaultPath + project.getPath() + file);
                    content = new String(Files.readAllBytes(Paths.get(defaultPath + project.getPath() + file)));
                }
                catch (IOException ie) {
                    message = "Error during reading file!";
                    success = false;
                }
            }
        }

        if (project_id <= 0) {
            success = false;
            message = "Invalid project id!";
        }

        return new ResponseEntity<>(new getFileResult(message, success, content), HttpStatus.OK);
    }

    @RequestMapping(value = "/saveAndCheck",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> saveAndCheck(@RequestParam String title,
                                              @RequestParam String desc,
                                              @RequestParam String attributes,
                                               @RequestParam long project_id,
                                               @RequestParam String files
    ) throws DockerException, InterruptedException, DockerCertificateException, IOException
    {
        System.out.println("Files inner: " + files);
        Type listType = new TypeToken<List<Wrapper>>() {}.getType();
        Gson g = new Gson();
        List<Wrapper> listFiles = g.fromJson(files, listType);
        /*for (Wrapper w : listFiles) {
            System.out.println(w);
        }*/

        Project project;
        boolean success = true;
        String message = "There must be result of check by borealis.";
        if (project_id > 0) {
            project = projectsService.selectProject(project_id);
            project_id = project.getId();
            if (project_id > 0) {
                project.setTitle(title);
                project.setDescription(desc);
                projectsService.updateProject(project_id, project);
                try {
                    for (Wrapper w : listFiles) {
                        FileUtils.writeStringToFile(new File(defaultPath + project.getPath() + w.file), w.content);
                    }
                }
                catch (IOException ie) {
                    message = "Error during writing files!";
                    success = false;
                }
                DockerController dc = new DockerController();
                String path = defaultPath + project.getPath();
                File dir = new File(path);
                String gitDir = dir.getAbsolutePath() + File.separator + ".git";
                ArrayList<ElementHierarchy> hierarchy = new ArrayList<ElementHierarchy>();
                list(dir, gitDir, hierarchy, 0, "");
                /*
                message = "";
                for (ElementHierarchy elem: hierarchy) {
                    if (elem.isFile && elem.extension.equals("c")) {
                        message += dc.makeCheck(path + elem.parent, elem.name);
                    }
                }
                 */
                //message = dc.makeCheckFiles(path, hierarchy);
                String filesC = explodeFilesC(dir, gitDir, "", 0);
                message = dc.makeCheck(path, filesC);
            }
        }

        /*if (success == true) {
            message = "There must be result of check by borealis.";
        }*/

        return new ResponseEntity<>(new CheckResult(message, success), HttpStatus.OK);
    }

    @RequestMapping(value = "/addAndCheck",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> addAndCheck(@RequestParam String title,
                                                          @RequestParam String desc,
                                                          @RequestParam String owner,
                                                          @RequestParam String repo,
                                                          @RequestParam String commitOrBranch,
                                                          @RequestParam String type,
                                                          @RequestParam String username,
                                                          @RequestParam String password,
                                                          @RequestParam String attributes
    ) {
        boolean success = true, result;
        String message = "";
        if (owner.length() == 0 || repo.length() == 0) {
            success = false;
            message = "You must specify owner and repository name!";
        }

        GitHubClient client = new GitHubClient();
        if (username.length() > 0 && password.length() > 0) {
            client.setCredentials(username, password);
        }

        String commit = "", branch = "";
        if (commitOrBranch.length() == 0) {
            commit = getBranchLastCommitSha(owner, repo, "master", client);
        }
        else if (type.equals("1")) {
            branch = commitOrBranch;
            commit = getBranchLastCommitSha(owner, repo, branch, client);
        }
        else {
            commit = commitOrBranch;
        }
        System.out.println("Commit: " + commit);
        String relativePath = owner + "_" + repo + "_" + commit;
        String localPath = defaultPath + relativePath;

        if (success == true) {
            result = cloneRepo(owner, repo, commit, branch, client, localPath);
            if (result == false) {
                message = "There was a problem with cloning the repository. If the repository is private you must fill username and password! Please, check owner, repository name, commit or branch.";
                success = false;
            }
            else {
                message = "There must be result of check by borealis.";
            }
        }
        if (success == true) {
            Project project = new Project(
                    title, desc, owner, repo, commit, branch, relativePath, 1
            );
            /*User user = new User();
            user.setUsername("gg");
            user.setPassword("g");
            authService.insertUser(user);*/
            projectsService.insertProject(project);
        }
        return new ResponseEntity<>(new CheckResult(message, success), HttpStatus.OK);
    }
/*public @ResponseBody Greeting sayHello(@RequestParam(value="name", required=false, defaultValue="Stranger") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }
*/

    public String getBranchLastCommitSha(String owner, String repo, String branch, GitHubClient client)
    {
        RepositoryService service = new RepositoryService(client);
        CommitService commitService = new CommitService(client);
        String sha = "";

        try {
            Repository r = service.getRepository(owner, repo);
            RepositoryCommit repoCommit = commitService.getCommits(r, branch, null).get(0);
            sha = repoCommit.getSha();
        }
        catch(IOException ie) {
            System.out.println("Error with get repository!\n");
        }

        return sha;
    }

    public void testSomeMethod()
    {
        GitHubClient client = new GitHubClient();
        //client.setCredentials(username, password);

        RepositoryService service = new RepositoryService(client);
        PullRequestService service1 = new PullRequestService(client);
        //cloneRepo("MashaFomina", "DB-labs", client);

//service1.
        //try {
            /*List<Repository> portal.repositories = service.getRepositories();
            CommitService commitService = new CommitService(client);
            for (int i = 0; i < portal.repositories.size(); i++) {
                Repository repo = portal.repositories.get(i);
                //Repository repository = .getRepository("https://github.com/MashaFomina/fp_labs");
                System.out.println("Repository Name: " + repo.getName());
                try {
                    for (RepositoryCommit commit : commitService.getCommits(repo)) {
                        //commit.getFiles().
                        Commit temp = commit.getCommit();

                        System.out.println("Commit SHA:\n" + temp.getTree().getSha() + "\nMessage: " + temp.getMessage());
                        System.out.println("Url: " + commit.getUrl());
                        System.out.println("Url: " + temp.getUrl());
                    }
                }
                catch(IOException ie) {
                    System.out.println("Error with getCommits of " + repo.getName() + "!\n");
                }*/
                /*try {
                    // now contents service
                    ContentsService contentService = new ContentsService(client);
                    List<RepositoryContents> test = contentService.getContents(repo);
                    DownloadResource d = new DownloadResource();
                    Download dd = new Download();

                    for (RepositoryContents content : test) {
                        String fileContent = content.getContent();
                        System.out.println("Size: " + fileContent.length());
                        FileOutputStream fos = new FileOutputStream("name_t");
                        System.out.println("Name: " + content.getName());
                        System.out.println("Length:" + fileContent.getBytes().length);
                        fos.write(fileContent.getBytes());
                        fos.close();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }*/
            /*}
        }
        catch(IOException ie) {
            System.out.println("Error with getRepositories!\n");
        }*/
    }

    private static boolean cloneRepo(String owner, String repo, String commit, String branch, GitHubClient client, String localPath)
    {
        boolean result = true;
        Git git = null;
        File dir = new File(localPath);
        try {
            RepositoryService rs = new RepositoryService(client);
            Repository r = rs.getRepository(owner, repo);

            String cloneURL = r.getCloneUrl();
            /*
            System.out.println(r.getSshUrl()); // git@github.com:MashaFomina/DB-labs.git, problem with UnknownHostKey: github.com, ssh public key
            // Two variants works
            System.out.println(r.getCloneUrl()); //https://github.com/MashaFomina/DB-labs.git
            System.out.println(r.getGitUrl()); // git://github.com/MashaFomina/DB-labs.git
            */

            // prepare a new folder for the cloned repository
            if (dir.isDirectory() != false) {
                FileUtils.deleteDirectory(dir);
            }
            dir.mkdirs();

            // Clone the repository
            CloneCommand command = Git.cloneRepository();
            command = Git.cloneRepository()
                    .setURI(cloneURL)
                    .setDirectory(dir);
            if (branch.length() > 0) {
                command.setBranch(branch);
            }
            git = command.call();

            // Checkout on commit
            if (commit.length() > 0) {
                git.checkout().setName(commit).call();
            }
            //System.out.println("Branch: " + git.getRepository().getBranch());
        } catch (IOException | GitAPIException ex) {
            System.out.println("There was a problem with cloning the repository: " + ex.getMessage());
            result = false;
        } finally {
            if (git != null) {
                git.close();
            }

            if (result == false) {
                if (dir.isDirectory() != false) {
                    try {
                        FileUtils.deleteDirectory(dir);
                    }
                    catch (IOException ex) {}
                }
            }
        }
        /**
         * Get tree with given SHA-1
         *
         * @param repository
         * @param sha
         * @param recursive
         * @return tree
         * @throws IOException
         */
        /*public Tree getTree(IRepositoryIdProvider repository, String sha,
        boolean recursive) throws IOException {
            final String id = getId(repository);
            if (sha == null)
                throw new IllegalArgumentException("SHA-1 cannot be null"); //$NON-NLS-1$
            if (sha.length() == 0)
                throw new IllegalArgumentException("SHA-1 cannot be empty"); //$NON-NLS-1$

            StringBuilder uri = new StringBuilder();
            uri.append(SEGMENT_REPOS);
            uri.append('/').append(id);
            uri.append(SEGMENT_GIT);
            uri.append(SEGMENT_TREES);
            uri.append('/').append(sha);
            GitHubRequest request = createRequest();
            request.setType(Tree.class);
            request.setUri(uri);
            if (recursive)
                request.setParams(Collections.singletonMap("recursive", "1")); //$NON-NLS-1$ //$NON-NLS-2$
            return (Tree) client.get(request).getBody();
        }*/
        return result;
    }
}