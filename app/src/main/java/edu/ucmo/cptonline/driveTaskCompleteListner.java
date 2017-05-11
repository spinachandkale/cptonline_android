package edu.ucmo.cptonline;

/**
 * Created by avina on 5/11/2017.
 */

public interface driveTaskCompleteListner<T> {
    public void onDriveTaskComplete(T result, Boolean statusChange);
}
