package calplug.bluetoothsri.Utility;


import calplug.bluetoothsri.bluetoothUtility.ConnectionHandler;

import android.view.View;
import android.view.View.OnClickListener;

// This class is imported from Sahil's code

public abstract class AlternateFunctionListener implements OnClickListener {

    @Override
    public void onClick(View v) {
        if (ConnectionHandler.getInstance().isConnected()) {
            OnClickSecondary();
        } else {
            OnClickPrimary();
        }
    }
    /**
     * Action when device is disconnected.
     */
    public abstract void OnClickPrimary();

    /**
     * Action when device is connected.
     */
    public abstract void OnClickSecondary();

}
