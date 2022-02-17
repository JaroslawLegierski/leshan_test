/*******************************************************************************
 * Copyright (c) 2021 Sierra Wireless and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *******************************************************************************/
function getMode(sec) {
  if (sec.x509) return "x509";
  else if (sec.psk) return "psk";
  else if (sec.rpk) return "rpk";
  else return "unsupported";
}

function getModeIcon(mode) {
  switch (mode) {
    case "x509":
      return "mdi-certificate";
    case "psk":
      return "mdi-lock";
    case "rpk":
      return "mdi-key-change";
    default:
      return "mdi-help-rhombus-outline";
  }
}

export { getMode, getModeIcon };
