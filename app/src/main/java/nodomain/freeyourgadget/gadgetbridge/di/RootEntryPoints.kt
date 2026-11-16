package nodomain.freeyourgadget.gadgetbridge.di

import android.app.Application
import dagger.hilt.EntryPoints

/**
 * The static way into the app's Hilt component, for construction roots: classes Hilt can't build that have no
 * Context, such as model classes created with `new`. A root does one lookup and passes what it gets on through
 * constructors, so only roots call this. A root that has a Context uses
 * [dagger.hilt.android.EntryPointAccessors] with it instead.
 *
 * The application registers itself in `onCreate` and unregisters in `onTerminate`.
 */
object RootEntryPoints {
    @Volatile
    private var application: Application? = null

    fun register(application: Application) {
        this.application = application
    }

    fun unregister() {
        application = null
    }

    /**
     * Returns [entryPoint] from the registered application's component.
     *
     * @throws IllegalStateException if no application is registered.
     */
    fun <T : Any> get(entryPoint: Class<T>): T {
        val application = checkNotNull(application) {
            "Can't get ${entryPoint.name}: no application is registered with RootEntryPoints"
        }
        return EntryPoints.get(application, entryPoint)
    }
}
