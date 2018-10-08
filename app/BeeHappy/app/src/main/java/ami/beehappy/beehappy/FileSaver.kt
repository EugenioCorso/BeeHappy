package ami.beehappy.beehappy
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import android.os.Environment
import android.os.Environment.*
import android.util.Log
import java.security.AccessControlContext

class FileSaver() {

    // check if external storage is readable / writable
    private fun isExternalStorageWritable(): Boolean {
        val state = getExternalStorageState();
        if (MEDIA_MOUNTED.equals(state)) {
            return true
        }
        return false
    }

    private fun isExternalStorageReadable(): Boolean {
        val state = getExternalStorageState();
        if (MEDIA_MOUNTED.equals(state) || MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true
        }
        return false
    }

    fun getAlbumStorageDir(albumName: String): File {
        // Get the directory for the user's public pictures directory.
        val dir: File = File(getExternalStoragePublicDirectory(DIRECTORY_PICTURES), albumName )
        if(!dir.mkdirs()){
            // TODO: should notify the error
        }
        return dir
    }

    fun saveFile(context: Context, fileBuf:  ByteArray, filepath: String) {
        // save a file from a stream supplied
        val outputStream: FileOutputStream
        // here we use an output stream to write the file buffer to the filename given
        // context is necessary here
        try {
            outputStream = context.openFileOutput(filepath, Context.MODE_PRIVATE);
            outputStream.write(fileBuf);
            outputStream.close();
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }
}