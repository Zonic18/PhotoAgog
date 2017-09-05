package zonic.photoagog.utils;

import java.io.File;

/**
 * Created by maithani on 21-08-2017.
 */

public abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}
