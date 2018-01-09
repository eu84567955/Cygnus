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
package center;

import center.packet.CenterPacket;
import io.netty.channel.Channel;
import java.util.ArrayList;
import netty.InPacket;
import netty.Socket;

/**
 *
 * @author Kaz Voeten
 */
public class CCenterSocket extends Socket {

    public int nWorldID;
    public String sWorldName, sMessage;
    public byte nState;
    public short nExp, nDrop;
    public boolean bCreateChar;
    public ArrayList<GameServer> aChannels = new ArrayList<>();

    public CCenterSocket(Channel channel, int uSeqSend, int uSeqRcv) {
        super(channel, uSeqSend, uSeqRcv);
    }

    public void ProcessPacket(CenterPacket nPacketID, InPacket iPacket) {
        switch (nPacketID) {
            case WorldInformation:
                this.nWorldID = iPacket.DecodeInteger(); //Max 44 atm
                this.sWorldName = iPacket.DecodeString();
                this.sMessage = iPacket.DecodeString();
                this.nState = iPacket.DecodeByte();
                this.nExp = iPacket.DecodeShort();
                this.nDrop = iPacket.DecodeShort();
                this.bCreateChar = iPacket.DecodeBoolean();
                break;
            case ChannelInformation:
                aChannels.clear();
                for (int i = iPacket.Decode(); i > 0; --i) {
                    aChannels.add(GameServer.Decode(iPacket));
                }
                break;
            default:
                break;
        }
    }
}
