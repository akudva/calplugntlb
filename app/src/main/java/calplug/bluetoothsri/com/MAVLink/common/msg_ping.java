/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE PING PACKING
package calplug.bluetoothsri.com.MAVLink.common;
import calplug.bluetoothsri.com.MAVLink.MAVLinkPacket;
import calplug.bluetoothsri.com.MAVLink.Messages.MAVLinkMessage;
import calplug.bluetoothsri.com.MAVLink.Messages.MAVLinkPayload;
        
/**
* Ping from one component to another
*/
public class msg_ping extends MAVLinkMessage{

    public static final int MAVLINK_MSG_ID_PING = 1;
    public static final int MAVLINK_MSG_LENGTH = 1;
    private static final long serialVersionUID = MAVLINK_MSG_ID_PING;


      
    /**
    * Number to identify ping in a sequence
    */
    public short sequence_number;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_PING;
              
        packet.payload.putUnsignedByte(sequence_number);
        
        return packet;
    }

    /**
    * Decode a ping message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
        this.sequence_number = payload.getUnsignedByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_ping(){
        msgid = MAVLINK_MSG_ID_PING;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_ping(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_PING;
        unpack(mavLinkPacket.payload);        
    }

      
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_PING -"+" sequence_number:"+sequence_number+"";
    }
}
        