package zonic.photoagog.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by maithani on 21-08-2017.
 */

public class BaseAlbumDirFactory extends AlbumStorageDirFactory {
    private static final String CAMERA_DIR = "/xaidworkz/";

    @Override
    public File getAlbumStorageDir(String albumName) {
        return new File (
                Environment.getExternalStorageDirectory()
                        + CAMERA_DIR
                        + albumName
        );
    }
}
