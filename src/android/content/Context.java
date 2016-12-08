package android.content;

/** Context that must be implemented for the library to interface with surrounding systems. */
public abstract class Context {
    public abstract String getPackageName();
    public abstract String getDataDir();
    public abstract android.graphics.drawable.Drawable getIcon();
    public abstract String getApplicationName();
}
