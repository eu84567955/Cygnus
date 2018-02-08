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
package character;

import character.inventory.ItemSlotIndex;
import database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import net.InPacket;
import net.OutPacket;

/**
 *
 * @author Kaz Voeten
 */
public class AvatarLook {

    public final int dwCharacterID;
    public byte nGender = 1; //for zero/ab
    public byte nSkin = 0;
    public int nFace = 0;
    public int nHair = 0;
    public int nJob = 0;
    public int nWeaponsStickerID = 0;
    public int nWeaponID = 0;
    public int nSubWeaponID = 0;
    public boolean bDrawElfEar = false;
    public int nXenonDefFaceAcc = 0;
    public int nDemonSlayerDefFaceAcc = 0;
    public int nBeastDefFaceAcc = 0;
    public int nBeastEars = 5010116;
    public int nBeastTail = 5010119;
    public byte nMixedHairColor = 0;
    public byte nMixHairPercent = 0;
    public int[] pets;
    public Map<Byte, Integer> anEquip = new HashMap<>();

    public AvatarLook(int dwCharacterID) {
        this.dwCharacterID = dwCharacterID;
    }

    public void SaveNew() {
        try (Connection c = Database.GetConnection()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO AvatarLook (dwCharacterID, nGender, nSkin, nFace, nHair, "
                    + "nJob, nWeaponsStickerID, nWeaponID, nSubWeaponID, bDrawElfEar, nXenonDefFaceAcc, nDemonSlayerDefFaceAcc, nBeastDefFaceAcc, nBeastEars, nBeastTail, nMixedHairColor, nMixHairPercent) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            ps.setInt(1, dwCharacterID);
            ps.setByte(2, nGender);
            ps.setByte(3, nSkin);
            ps.setInt(4, nFace);
            ps.setInt(5, nHair);
            ps.setInt(6, nJob);
            ps.setInt(7, nWeaponsStickerID);
            ps.setInt(8, nWeaponID);
            ps.setInt(9, nSubWeaponID);
            ps.setBoolean(10, bDrawElfEar);
            ps.setInt(11, nXenonDefFaceAcc);
            ps.setInt(12, nDemonSlayerDefFaceAcc);
            ps.setInt(13, nBeastDefFaceAcc);
            ps.setInt(14, nBeastEars);
            ps.setInt(15, nBeastTail);
            ps.setInt(16, nMixedHairColor);
            ps.setInt(17, nMixHairPercent);
            ps.execute();
            ps.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static AvatarLook LoadAvatarLook(int dwCharacterID) {
        AvatarLook ret = new AvatarLook(dwCharacterID);
        try (Connection c = Database.GetConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM AvatarLook WHERE dwCharacterID = ?");
            ps.setInt(1, dwCharacterID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ret.nGender = rs.getByte("nGender");
                ret.nSkin = rs.getByte("nSkin");
                ret.nFace = rs.getInt("nFace");
                ret.nHair = rs.getInt("nHair");
                ret.nJob = rs.getInt("nJob");
                ret.nWeaponsStickerID = rs.getInt("nWeaponsStickerID");
                ret.nWeaponID = rs.getInt("nWeaponID");
                ret.nSubWeaponID = rs.getInt("nSubWeaponID");
                ret.bDrawElfEar = rs.getBoolean("bDrawElfEar");
                ret.nXenonDefFaceAcc = rs.getInt("nXenonDefFaceAcc");
                ret.nDemonSlayerDefFaceAcc = rs.getInt("nDemonSlayerDefFaceAcc");
                ret.nBeastDefFaceAcc = rs.getInt("nBeastDefFaceAcc");
                ret.nBeastEars = rs.getInt("nBeastEars");
                ret.nBeastTail = rs.getInt("nBeastTail");
                ret.nMixedHairColor = rs.getByte("nMixedHairColor");
                ret.nMixHairPercent = rs.getByte("nMixHairPercent");
            }

            ps = c.prepareStatement("SELECT nItemID, nSlot FROM GW_ItemSlotEquip WHERE dwCharacterID = ?");
            ps.setInt(1, dwCharacterID);
            rs = ps.executeQuery();

            while (rs.next()) {
                ret.anEquip.put((byte) rs.getInt("nSlot"), rs.getInt("nItemID"));
            }

            try {
                ps = c.prepareStatement("SELECT * FROM GW_ItemSlotEquip WHERE dwCharacterID = ?");
                ps.setInt(1, dwCharacterID);
                rs = ps.executeQuery();

                while (rs.next()) {
                    int nSlot = rs.getInt("nSlot");
                    int nItemID = rs.getInt("nItemID");
                    if (nSlot > 0) {
                        ret.anEquip.put((byte) nSlot, nItemID);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void Encode(OutPacket oPacket) {
        Encode(oPacket, null);
    }

    public void Encode(OutPacket oPacket, ZeroInfo pZero) {
        oPacket.EncodeByte(pZero == null ? nGender : 1);
        oPacket.EncodeByte(pZero == null ? nSkin : pZero.nSubSkin);
        oPacket.EncodeInt(pZero == null ? nFace : pZero.nSubFace);
        oPacket.EncodeInt(nJob);
        oPacket.EncodeBool(false);
        oPacket.EncodeInt(pZero == null ? nHair : pZero.nSubHair);

        int[] anHairEquip = new int[ItemSlotIndex.BP_COUNT];
        int[] anUnseenEquip = new int[ItemSlotIndex.BP_COUNT];
        int[] anVirtualEquip = new int[ItemSlotIndex.BP_COUNT];
        for (Map.Entry<Byte, Integer> item : anEquip.entrySet()) {
            int nPos = item.getKey();
            if (nPos < ItemSlotIndex.BP_COUNT) {
                if (anHairEquip[nPos] == 0) {
                    anHairEquip[nPos] = item.getValue();
                }
                if (anHairEquip[nPos] != 0 && nPos != ItemSlotIndex.BP_WEAPON) {
                    anUnseenEquip[nPos] = anHairEquip[nPos];
                    anVirtualEquip[nPos] = anHairEquip[nPos];//IDK FO SURE FAM
                }
            }
        }

        int i = 1; //Starts at cap, not hair :3
        do {
            if (anHairEquip[i] != 0) {
                oPacket.EncodeByte(i).EncodeInt(anHairEquip[i]);
            }
            ++i;
        } while (i < ItemSlotIndex.BP_COUNT);
        oPacket.EncodeByte(0xFF);

        i = 1;
        do {
            if (anUnseenEquip[i] != 0) {
                oPacket.EncodeByte(i).EncodeInt(anUnseenEquip[i]);
            }
            ++i;
        } while (i < ItemSlotIndex.BP_COUNT);
        oPacket.EncodeByte(0xFF);

        i = 1;
        do {
            if (anVirtualEquip[i] != 0) {
                oPacket.EncodeByte(i).EncodeInt(anVirtualEquip[i]);
            }
            ++i;
        } while (i < ItemSlotIndex.BP_COUNT);
        oPacket.EncodeByte(0xFF);

        oPacket.EncodeInt(nWeaponsStickerID);
        oPacket.EncodeInt(pZero == null ? anHairEquip[ItemSlotIndex.BP_WEAPON] : pZero.nLazuli);
        oPacket.EncodeInt(anHairEquip[ItemSlotIndex.BP_SHIELD]);

        oPacket.EncodeBool(bDrawElfEar);
        oPacket.EncodeBool(false);//new

        //TODO: Pets
        for (i = 0; i < 3; i++) {
            oPacket.EncodeInt(0); //Just pet ID lol.
        }

        //TODO: make face acc. part of unseen equip inventory properly.
        if (nJob / 100 != 31 && nJob != 3001) {
            if (nJob / 100 != 36 && nJob != 3002) {
                if (nJob != 10000 && nJob != 10100 && nJob != 10110 && nJob != 10111 && nJob != 10112) {
                    if (GW_CharacterStat.IsBeastJob(nJob)) {
                        oPacket.EncodeInt(nBeastDefFaceAcc);
                        oPacket.EncodeBool(true).EncodeInt(nBeastEars);
                        oPacket.EncodeBool(true).EncodeInt(nBeastTail);
                    }
                } else {
                    oPacket.EncodeBool(pZero != null);
                }
            } else {
                oPacket.EncodeInt(nXenonDefFaceAcc);
            }
        } else {
            oPacket.EncodeInt(nDemonSlayerDefFaceAcc);
        }
        oPacket.EncodeByte(nMixedHairColor);
        oPacket.EncodeByte(nMixHairPercent);
    }

    public static AvatarLook Decode(int nCharacterID, InPacket iPacket) {
        AvatarLook ret = new AvatarLook(nCharacterID);
        ret.nGender = iPacket.DecodeByte();
        ret.nSkin = iPacket.DecodeByte();
        ret.nFace = iPacket.DecodeInt();
        ret.nJob = iPacket.DecodeInt();
        iPacket.DecodeByte();
        ret.nHair = iPacket.DecodeInt();

        byte nPos = iPacket.DecodeByte();
        while (nPos != (byte) 0xFF) {
            ret.anEquip.put(nPos, iPacket.DecodeInt());
            nPos = iPacket.DecodeByte();
        }
        nPos = iPacket.DecodeByte();
        while (nPos != (byte) 0xFF) {
            ret.anEquip.put(nPos, iPacket.DecodeInt());
            nPos = iPacket.DecodeByte();
        }
        nPos = iPacket.DecodeByte();
        while (nPos != (byte) 0xFF) {
            ret.anEquip.put(nPos, iPacket.DecodeInt());
            nPos = iPacket.DecodeByte();
        }

        ret.nWeaponsStickerID = iPacket.DecodeInt();
        ret.nWeaponID = iPacket.DecodeInt();
        ret.nSubWeaponID = iPacket.DecodeInt();
        ret.bDrawElfEar = iPacket.DecodeBool();
        iPacket.DecodeBool(); //Welp, which job has a new thing?

        for (int i = 0; i < 3; i++) {
            iPacket.DecodeInt();
        }

        if (ret.nJob / 100 != 31 && ret.nJob != 3001) {
            if (ret.nJob / 100 != 36 && ret.nJob != 3002) {
                if (ret.nJob != 10000 && ret.nJob != 10100 && ret.nJob != 10110 && ret.nJob != 10111 && ret.nJob != 10112) {
                    if (GW_CharacterStat.IsBeastJob(ret.nJob)) {
                        ret.nBeastDefFaceAcc = iPacket.DecodeInt();
                        iPacket.DecodeByte();
                        ret.nBeastEars = iPacket.DecodeInt();
                        iPacket.DecodeByte();
                        ret.nBeastTail = iPacket.DecodeInt();
                    }
                } else {
                    iPacket.DecodeBool();//True if zero char.
                }
            } else {
                ret.nXenonDefFaceAcc = iPacket.DecodeInt();
            }
        } else {
            ret.nDemonSlayerDefFaceAcc = iPacket.DecodeInt();
        }
        ret.nMixedHairColor = iPacket.DecodeByte();
        ret.nMixHairPercent = iPacket.DecodeByte();
        return ret;
    }

}
