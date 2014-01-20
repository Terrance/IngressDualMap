package to.uk.terrance.ingressdualmap;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class NotificationActivity extends Activity {

    public static final String ACTION_OPTS = "opts";
    public static final String ACTION_HACK = "hack";
    public static final String ACTION_RESET = "reset";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] params = getIntent().getAction().substring(Utils.PACKAGE.length() + 1).split("\\.");
        String action = params[0];
        final int i = Integer.valueOf(params[1]);
        Log.e("IDM_Location", i + "/" + Portal.PORTALS.size());
        final Portal portal = Portal.PORTALS.get(i);
        if (action.equals("opts")) {
            new AlertDialog.Builder(this)
                .setTitle(portal.getName())
                .setItems(new String[]{
                    "Mark hacked",
                    "Burned out",
                    "Reset status",
                    (portal.isPinned() ? "Unpin" : "Pin") + " notification"
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                hackPortal(i, portal);
                                break;
                            case 1:
                                burnOutPortal(i, portal);
                                break;
                            case 2:
                                resetPortal(i, portal);
                                break;
                            case 3:
                                pinPortal(i, portal);
                                break;
                        }
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                })
                .create()
                .show();
        } else if (action.equals("hack")) {
            Log.i("IDM_Location", portal.toString());
            hackPortal(i, portal);
            finish();
        } else if (action.equals("reset")) {
            resetPortal(i, portal);
            finish();
        }
    }

    public void hackPortal(int i, Portal portal) {
        int hacks = portal.getHacksRemaining() - 1;
        portal.setHacksRemaining(hacks);
        String message = "Hacked " + portal.getName() + ".\n";
        if (hacks > 0) {
            portal.setRunningHot();
            Log.d("IDM_Location", "" + portal.checkRunningHot());
            message += hacks + " hack" + Utils.plural(hacks) + " remaining before burnout.";
        } else {
            portal.setBurnedOut();
            message += "Portal burnt out!";
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        LocationService.notifyPortal(this, i, true);
    }

    public void burnOutPortal(int i, Portal portal) {
        portal.setBurnedOut();
        Toast.makeText(this, portal.getName() + " burned out.", Toast.LENGTH_LONG).show();
        LocationService.notifyPortal(this, i, true);
    }

    public void resetPortal(int i, Portal portal) {
        portal.reset();
        Toast.makeText(this, "Reset " + portal.getName() + ".", Toast.LENGTH_LONG).show();
        LocationService.notifyPortal(this, i, true);
    }

    public void pinPortal(int i, Portal portal) {
        portal.setPinned(!portal.isPinned());
        LocationService.notifyPortal(this, i, (portal.getDistance() <= 50 || portal.isPinned()));
    }

}
