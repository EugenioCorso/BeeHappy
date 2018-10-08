package ami.beehappy.beehappy

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder

class NotificationHelper (context: Context) {

    var actualContext: Context = context

    fun notifyEvent (text: String){
        var builder: NotificationCompat.Builder = NotificationCompat.Builder(this.actualContext).setSmallIcon( R.drawable.notification_icon_background).setContentTitle("BeeHappy").setContentText(text)
        // Creates an explicit intent for an Activity in your app
        var resultIntent: Intent = Intent(this.actualContext, DisplayActivity::class.java)

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        var stackBuilder: TaskStackBuilder = TaskStackBuilder.create(this.actualContext)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(DisplayActivity::class.java)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)

        var resultPendingIntent: PendingIntent  = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(resultPendingIntent)

        var mNotificationManager: NotificationManager = this.actualContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mId allows you to update the notification later on.
        var mId = 0;
        mNotificationManager.notify(mId, builder.build());
    }
}