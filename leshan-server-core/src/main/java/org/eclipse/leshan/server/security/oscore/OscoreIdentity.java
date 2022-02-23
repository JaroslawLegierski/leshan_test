/*******************************************************************************
 * Copyright (c) 2022    Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.server.security.oscore;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.leshan.core.util.Hex;

/**
 * An OSCORE identifier for a foreign peer.
 *
 */
public class OscoreIdentity implements Serializable {

    private static final long serialVersionUID = 1L;
    protected final byte[] senderId;
    protected final byte[] recipientId;

    public OscoreIdentity(byte[] senderId, byte[] recipientId) {
        this.senderId = senderId;
        this.recipientId = recipientId;
    }

    // TODO OSCORE not sure we need this constructor
    public OscoreIdentity(String senderId, String recipientId) {
        this.senderId = new Hex().decode(senderId.getBytes(StandardCharsets.UTF_8));
        this.recipientId = new Hex().decode(recipientId.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] getSenderId() {
        return senderId;
    }

    public byte[] getRecipientId() {
        return recipientId;
    }

    // TODO OSCORE: Generate toString() in Eclipse.
    @Override
    public String toString() {
        return String.format("OscoreIdentity [senderId=%s, recipientId=%s]", Hex.encodeHexString(senderId),
                Hex.encodeHexString(recipientId));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(recipientId);
        result = prime * result + Arrays.hashCode(senderId);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OscoreIdentity other = (OscoreIdentity) obj;
        if (!Arrays.equals(recipientId, other.recipientId))
            return false;
        if (!Arrays.equals(senderId, other.senderId))
            return false;
        return true;
    }
}