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
package netty;

import crypto.CAESCipher;
import crypto.CIGCipher;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 *
 * @author Kaz Voeten
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext chc, Packet oPacket, ByteBuf oBuffer) throws Exception {
        Socket pSocket = chc.channel().attr(Socket.SESSION_KEY).get();
        byte[] aSendBuff = oPacket.GetData();

        if (pSocket != null) {
            
            if(!pSocket.bEncryptData) {
                int nLen = aSendBuff.length;
                oBuffer.writeInt(nLen);
                oBuffer.writeBytes(aSendBuff);
                return;
            }

            int dwKey = pSocket.uSeqSend;
            byte[] aHeader = CAESCipher.GetHeader(aSendBuff.length, dwKey);

            pSocket.Lock();
            try {
                if (pSocket.nCryptoMode == 1) {
                    CAESCipher.Crypt(aSendBuff, dwKey);
                } else if (pSocket.nCryptoMode == 2) {
                    CIGCipher.Encrypt(aSendBuff, dwKey);
                }
                pSocket.uSeqSend = CIGCipher.InnoHash(dwKey, 4, 0);
            } finally {
                pSocket.Unlock();
            }

            oBuffer.writeBytes(aHeader);
            oBuffer.writeBytes(aSendBuff);

        } else {
            oBuffer.writeBytes(aSendBuff);
        }
    }
}
