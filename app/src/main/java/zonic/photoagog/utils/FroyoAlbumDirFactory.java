package zonic.photoagog.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by maithani on 21-08-2017.
 */

public class FroyoAlbumDirFactory extends AlbumStorageDirFactory{
    @Override
    public File getAlbumStorageDir(String albumName) {
        return new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                albumName
        );
    }
}
