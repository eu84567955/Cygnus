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
package client;

import client.packet.ClientPacket;
import client.packet.Login;
import io.netty.channel.Channel;
import io.netty.util.concurrent.ScheduledFuture;
import net.InPacket;

import net.Socket;
import util.HexUtils;

/**
 *
 * @author Kaz Voeten
 */
public class ClientSocket extends Socket {

    public long nSessionID = 0;
    public int nWorldID = -1;
    public int nChannelID = -1;
    public int nCharacterSlots = 15;//TODO
    private long nLastAliveAck = -1;
    public Account pAccount;

    public ScheduledFuture<?> PingTask;

    public ClientSocket(Channel channel, int uSeqSend, int uSeqRcv) {
        super(channel, uSeqSend, uSeqRcv);
    }

    public void ProcessPacket(InPacket iPacket) {
        short nPacketID = iPacket.DecodeShort();
        switch (nPacketID) {
            case ClientPacket.AliveAck:
                this.nLastAliveAck = System.currentTimeMillis();
                break; 
            case ClientPacket.NMCORequest:
                SendPacket(Login.NCMOResult());
                break;
            case ClientPacket.PrivateServerPacket:
                SendPacket(Login.PrivateServerPacket(iPacket.DecodeInt()));
                break;
            case ClientPacket.CheckHotfix:
                SendPacket(Login.ApplyHotFix());
                SendPacket(Login.SecurityPacket());
                SendPacket(Login.AuthenMessage());
                break;
            case ClientPacket.WorldInfoLogoutRequest:
            case ClientPacket.WorldInfoForShiningRequest:
                Login.OnWorldInformationRequest(this);
                break;
            case ClientPacket.ClientDumpLog:
                Login.OnClientDumpLog(iPacket);
                break;
            case ClientPacket.UserLimitRequest:
                SendPacket(Login.UserLimitResult(0));
                break;
            case ClientPacket.SelectWorld:
                Login.OnSelectWorld(this, iPacket);
                break;
            case ClientPacket.CheckDuplicatedID:
                Login.OnCheckDuplicatedID(this, iPacket);
                break;
            case ClientPacket.CreateNewCharacter:
                Login.OnCreateNewCharacter(this, iPacket);
                break;
            case ClientPacket.PermissionRequest:
            case ClientPacket.ClientLoadingState:
                break;
            case ClientPacket.SetSPW:
                Login.OnSetSPW(this, iPacket);
                break;
            default:
                System.out.println("[DEBUG] Received unhandled Client packet. nPacketID: " 
                        + nPacketID + ". Data: " 
                        + HexUtils.ToHex(iPacket.Decode(iPacket.GetRemainder())));
                break;
        }
    }
    
    public void AliveReq() {
        if (nLastAliveAck == -1 || nLastAliveAck + (20 * 1000) > System.currentTimeMillis()) {
            SendPacket(Login.AliveReq());
        } else {
            this.Close();
        }
    }
}
