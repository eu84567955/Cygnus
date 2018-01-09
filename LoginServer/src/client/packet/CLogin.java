/*
 * Copyright (C) 2018 Kaz Voeten
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client.packet;

import center.CenterSessionManager;
import client.CClientSocket;
import netty.InPacket;
import netty.OutPacket;
import netty.Packet;

/**
 *
 * @author Kaz Voeten
 */
public class CLogin {

    public static Packet AliveReq() {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.AliveReq.getValue());
        return oPacket.ToPacket();
    }

    public static Packet LastConnectedWorld(int world) {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.LastConnectedWorld.getValue());
        oPacket.EncodeInteger(world);
        return oPacket.ToPacket();
    }

    public static Packet RecommendWorldMessage(int world) {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.RecommendWorldMessage.getValue());
        oPacket.Encode(1);
        oPacket.EncodeInteger(world);
        oPacket.EncodeString("The greatest world for starting anew!");
        return oPacket.ToPacket();
    }

    public static Packet UserLimitResult(int status) {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.UserLimitResult.getValue());
        oPacket.EncodeShort(status);
        return oPacket.ToPacket();
    }

    public static Packet GetLoginFailed(int reason) {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.CheckPasswordResult.getValue());
        oPacket.Encode(reason);
        oPacket.Encode(0);
        oPacket.EncodeInteger(0);
        return oPacket.ToPacket();
    }

    public static Packet GetBanMessage(int reason, long time) {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.CheckPasswordResult.getValue());
        oPacket.Encode(2);
        oPacket.Encode(0);
        oPacket.EncodeInteger(0x00);
        oPacket.Encode(reason);
        oPacket.EncodeLong(time);
        return oPacket.ToPacket();
    }

    public static Packet CharacterBurning(byte nType, int dwCharacterID) {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.CharacterBurning.getValue());
        oPacket.Encode(nType);
        oPacket.EncodeInteger(dwCharacterID);
        return oPacket.ToPacket();
    }

    public static Packet SelectWorldResult() {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.SelectWorldResult.getValue());
        oPacket.Encode(true);
        return oPacket.ToPacket();
    }

    public static Packet DuplicateIDResponse(String name, boolean taken) {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.CheckDuplicatedIDResult.getValue());
        oPacket.EncodeString(name);
        oPacket.Encode(!taken);
        return oPacket.ToPacket();
    }
    
    public static Packet SecurityPacket() {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.SecurityPacket.getValue());
        oPacket.Encode(0x01);
        return oPacket.ToPacket();
    }

    public static Packet ApplyHotFix() {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.ApplyHotFix.getValue());
        oPacket.Encode(true);
        return oPacket.ToPacket();
    }

    public static Packet NCMOResult() {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.NMCOResult.getValue());
        oPacket.Encode(true);
        return oPacket.ToPacket();
    }

    public static Packet PrivateServerPacket(int dwCurrentThreadID) {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.PrivateServerPacket.getValue());

        int response = dwCurrentThreadID ^ LoopBackPacket.PrivateServerPacket.getValue();
        oPacket.EncodeInteger(response);

        return oPacket.ToPacket();
    }

    public static Packet JobOrder() {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.JOB_ORDER.getValue());
        JobOrder.Encode(oPacket);
        return oPacket.ToPacket();
    }
    
    public static void OnWorldInformationRequest(CClientSocket pClient) {
        CenterSessionManager.aCenterSessions.forEach((pWorld) -> {
            OutPacket oPacket = new OutPacket();
            oPacket.EncodeShort(LoopBackPacket.WorldInformation.getValue());

            oPacket.Encode(pWorld.nWorldID);
            oPacket.EncodeString(pWorld.sWorldName);
            oPacket.Encode(pWorld.nState);
            oPacket.EncodeString(pWorld.sMessage);
            oPacket.Encode(pWorld.bCreateChar);

            oPacket.Encode(pWorld.aChannels.size());
            pWorld.aChannels.forEach((pChannel) -> {
                oPacket.EncodeString(pWorld.sWorldName + "-" + pChannel.nChannelID);
                oPacket.EncodeInteger(pChannel.nGaugePx);
                oPacket.Encode(pWorld.nWorldID);
                oPacket.Encode(pChannel.nChannelID);
                oPacket.Encode(pChannel.nChannelID - 1);
            });

            oPacket.EncodeShort(0); //Balloons lel
            oPacket.EncodeInteger(0);
            oPacket.Encode(0);

            pClient.SendPacket(oPacket.ToPacket());
        });

        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LoopBackPacket.WorldInformation.getValue());
        oPacket.Encode(0xFF);
        oPacket.Encode(0);
        oPacket.Encode(0);
        oPacket.Encode(0);
        pClient.SendPacket(oPacket.ToPacket());
    }

    public static void OnClientDumpLog(InPacket iPacket) {
        String sType = "Unknow report type";
        if (iPacket.Available() < 8) {
            System.out.println(sType + iPacket.DecodeString(iPacket.Available()));
        } else {
            switch (iPacket.DecodeShort()) {
                case 1:
                    sType = "Invalid Decoding";
                    break;
                case 2:
                    sType = "Crash Report";
                    break;
                case 3:
                    sType = "Exception";
                    break;
            }

            int nError = iPacket.DecodeInteger();
            short nLen = iPacket.DecodeShort();
            int tTimeStamp = iPacket.DecodeInteger();
            short nPacketID = iPacket.DecodeShort();

            String sPacketName = "Unk";
            for (LoopBackPacket packet : LoopBackPacket.values()) {
                if (packet.getValue() == (int) nPacketID) {
                    sPacketName = packet.name();
                }
            }

            iPacket.Reverse(2);
            Packet pPacket = new Packet(iPacket.GetRemainder());

            System.out.println(String.format("[Debug] Report type: %s \r\n\t   Error Num: %d, Data Length: %d \r\n\t   Account: %s \r\n\t   Opcode: %s, %d | %s \r\n\t   Data: %s",
                    sType, nError, nLen, "//", sPacketName, nPacketID, "0x" + Integer.toHexString(nPacketID), pPacket.toString()
            ));

        }
    }

    public class Balloon {

        public int nX;
        public int nY;
        public String sMessage;

        public Balloon(String sMessage, int nX, int nY) {
            this.sMessage = sMessage;
            this.nX = nX;
            this.nY = nY;
        }
    }
    
    /*
    public static Packet SelectWorldResult(Client c, List<AvatarData> avatars, boolean bIsEditedList) {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LP.SelectWorldResult.getValue());

        byte nDay = 0;
        oPacket.Encode(nDay);
        if (nDay == 61) {
            boolean SendOTPForWebLaunching = false;
            oPacket.Encode(SendOTPForWebLaunching);
        }
        oPacket.EncodeString(c.getWorldID() == 45 ? "reboot" : "normal");//topkek
        oPacket.EncodeInteger(c.getWorldID());// worldID?
        oPacket.Encode(false);//burning event blocked

        /**
         * if bigger than 0 write ReservedDeleteCharacter data, which writes
         * which character is gonna be deleted at which time if I ever wanna do
         * scheduled character deletions for limited access to pink bean or
         * someshit
        */
     /*
        oPacket.EncodeInteger(0);

        oPacket.EncodeInteger(0);//hightime
        oPacket.EncodeInteger(0);///lowtime

        oPacket.Encode(bIsEditedList); //bIsEditedList for after you reorganize the charlist
        Collections.sort(avatars, (AvatarData o1, AvatarData o2) -> o1.getCharListPosition() - o2.getCharListPosition());
        if (bIsEditedList) {
            oPacket.EncodeInteger(avatars.size());
            for (AvatarData avatar : avatars) {
                oPacket.EncodeInteger(avatar.getCharacterID());
            }
        } else {
            oPacket.EncodeInteger(0);//0 chars edited
        }

        oPacket.Encode((byte) avatars.size());
        for (AvatarData avatar : avatars) {
            avatar.Encode(oPacket, false);
        }

        oPacket.Encode((byte) 0); //bHasPic
        oPacket.Encode(false); //bQuerrySSNOnCreateNewCharacter LMAO LEZ STEAL THOSE NUMBERS
        oPacket.EncodeInteger(c.getCharacterSlots());
        oPacket.EncodeInteger(0);//amount of chars bought with CS coupons? nBuyCharCount
        oPacket.EncodeInteger(-1);//event new char job (maybe can be used for pinkbean)
        oPacket.EncodeInteger(0);//highTimeStamp
        oPacket.EncodeInteger(0);//lowTimeStamp
        oPacket.Encode((byte) 0); //enables the name change UI. value is count of names allowed to change
        oPacket.Encode(c.getWorldID() == 45); //based on world ID so might be reboot related

        return oPacket.ToPacket();
    }
    

    /*
    public static Packet CreateCharacterResult(AvatarData avatar, boolean success) {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LP.CreateCharacterResult);

        oPacket.Encode(!success);
        if (success) {
            avatar.Encode(oPacket, false);
        }

        return oPacket.ToPacket();
    }
     */

 /*
    public static Packet DeleteCharacterResult(int cid, int state) {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LP.DeleteCharacterResult);

        oPacket.EncodeInteger(cid);
        oPacket.Encode(state);

        return oPacket.ToPacket();
    }
     */

 /*
    public static Packet SelectCharacterResult(GameChannel gc, int cid) {
        OutPacket oPacket = new OutPacket();
        oPacket.EncodeShort(LP.SelectCharacterResult);

        oPacket.EncodeShort(0x00);
        oPacket.Encode(gc.IP);
        oPacket.EncodeShort(gc.PORT);
        oPacket.EncodeInteger(cid);
        oPacket.Fill(0x00, 5);

        return oPacket.ToPacket();
    }
     */
}
