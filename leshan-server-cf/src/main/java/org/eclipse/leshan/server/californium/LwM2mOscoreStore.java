/*******************************************************************************
 * Copyright (c) 2022 Sierra Wireless and others.
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
package org.eclipse.leshan.server.californium;

import org.eclipse.californium.cose.AlgorithmID;
import org.eclipse.californium.cose.CoseException;
import org.eclipse.leshan.core.californium.oscore.cf.OscoreParameters;
import org.eclipse.leshan.core.californium.oscore.cf.OscoreStore;
import org.eclipse.leshan.core.util.Validate;
import org.eclipse.leshan.server.security.SecurityInfo;
import org.eclipse.leshan.server.security.SecurityStore;
import org.eclipse.leshan.server.security.oscore.OscoreIdentity;

import com.upokecenter.cbor.CBORObject;

/**
 * An {@link OscoreStore} which search {@link OscoreParameters} in LWM2M {@link SecurityStore}
 */
public class LwM2mOscoreStore implements OscoreStore {

    private SecurityStore store;

    public LwM2mOscoreStore(SecurityStore securityStore) {
        Validate.notNull(securityStore);
        this.store = securityStore;
    }

    @Override
    public OscoreParameters getOscoreParameters(byte[] recipientID, byte[] senderID) {
        OscoreIdentity oscoreIdentity = new OscoreIdentity(senderID, recipientID);
        SecurityInfo securityInfo = store.getByOscoreIdentity(oscoreIdentity);
        if (securityInfo == null || !securityInfo.useOSCORE())
            return null;

        try {
            return new OscoreParameters(//
                    securityInfo.getOscoreSetting().getSenderId(), //
                    securityInfo.getOscoreSetting().getRecipientId(), //
                    securityInfo.getOscoreSetting().getMasterSecret(), //
                    AlgorithmID.FromCBOR(CBORObject.FromObject(securityInfo.getOscoreSetting().getAeadAlgorithm())), //
                    AlgorithmID.FromCBOR(CBORObject.FromObject(securityInfo.getOscoreSetting().getHmacAlgorithm())), //
                    securityInfo.getOscoreSetting().getMasterSalt());
        } catch (CoseException e) {
            // TODO OSCORE we need to think about how to manage this.
            throw new IllegalStateException("Unable to create Oscore Parameters", e);
        }
    }
}
