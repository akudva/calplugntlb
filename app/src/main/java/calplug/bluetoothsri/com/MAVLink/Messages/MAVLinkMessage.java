/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

package calplug.bluetoothsri.com.MAVLink.Messages;

import java.io.Serializable;

import calplug.bluetoothsri.com.MAVLink.MAVLinkPacket;

public abstract class MAVLinkMessage implements Serializable {
    private static final long serialVersionUID = -7754622750478538539L;
    // The MAVLink message classes have been changed to implement Serializable, 
    // this way is possible to pass a mavlink message trought the Service-Acctivity interface
    
    /**
     *  Simply a common interface for all MAVLink Messages
     */
    
    public  int sysid;
    public int compid;
    public int msgid;
    public abstract MAVLinkPacket pack();
    public abstract void unpack(MAVLinkPayload payload);
}
    