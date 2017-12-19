/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package portal.errors;

public class InvalidDataForSavingSeatsException extends Exception {
    public InvalidDataForSavingSeatsException() {
        super("Class number must be between 1 and 11, busy seats must be less or equals total seats!");
    }
}