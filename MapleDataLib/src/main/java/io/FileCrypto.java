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
package io;

import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Kaz Voeten
 */
public class FileCrypto {

    private final String sKey = "CygnusEmulator18";//16 chars / bytes
    private final SecretKeySpec secretKeySpec;
    private final IvParameterSpec IV;
    Cipher cipher;

    public FileCrypto(byte[] IV) throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.secretKeySpec = new SecretKeySpec(sKey.getBytes(), "AES");
        this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        this.IV = new IvParameterSpec(IV);
    }

    public byte[] Encrypt(byte[] data) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IV);
            return cipher.doFinal(data);
        } catch (Exception ex) {
            ex.printStackTrace();
            return data;
        }
    }

    public byte[] Decrypt(byte[] data) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IV);
            return cipher.doFinal(data);
        } catch (Exception ex) {
            ex.printStackTrace();
            return data;
        }
    }
}
