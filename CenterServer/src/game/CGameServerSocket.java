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
package game;

import game.packet.GamePacket;
import game.packet.LoopBackPacket;
import io.netty.channel.Channel;
import login.LoginSessionManager;
import login.packet.LLogin;
import netty.InPacket;
import netty.Packet;
import netty.Socket;
import server.Client;
import server.Configuration;

/**
 *
 * @author Kaz Voeten
 */
public class CGameServerSocket  extends Socket {
    public byte nChannelID;
    public int nMaxUsers;
    //public HashMap<Integer, User> mUsers = new HashMap<>();

    public CGameServerSocket(Channel channel, int uSeqSend, int uSeqRcv) {
        super(channel, uSeqSend, uSeqRcv);
    }
    
    @Override
    public void SendPacket(Packet oPacket) {
        if (Configuration.SERVER_CHECK) {
            String head = "Unk";
            for (LoopBackPacket packet : LoopBackPacket.values()) {
                if (packet.getValue() == (int) oPacket.GetHeader()) {
                    head = packet.name();
                    if (packet.getValue() == 0xF0) {
                        head = "HandShake/" + head;
                    }
                }
            }
            System.out.printf("[Debug] Sent %s: %s%n", head, oPacket.toString());
        }
        super.SendPacket(oPacket);
    }
    
    public void ProcessPacket(GamePacket nPacketID, InPacket iPacket) {
        switch(nPacketID) {
            case GameServerInformation:
                this.nChannelID = iPacket.DecodeByte();
                this.nMaxUsers = iPacket.DecodeByte();
                LLogin.GameServerInformation(LoginSessionManager.pSession);
                break;
        }
    }
}