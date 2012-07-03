package io.rocketeer.protocol.stomp;

/*
 * Copyright 2011 Red Hat, Inc, and individual contributors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jboss.netty.buffer.ChannelBuffers;

public class StompFrames {

    public static StompFrame newAckFrame(Headers headers) {
        StompControlFrame frame = new StompControlFrame( StompFrame.Command.ACK );
        frame.setHeader( StompFrame.Header.MESSAGE_ID, headers.get( StompFrame.Header.MESSAGE_ID ) );
        frame.setHeader( StompFrame.Header.SUBSCRIPTION, headers.get( StompFrame.Header.SUBSCRIPTION ) );
        String transactionId = headers.get( StompFrame.Header.TRANSACTION );
        if (transactionId != null) {
            frame.setHeader( StompFrame.Header.TRANSACTION, transactionId );
        }
        return frame;
    }

    public static StompFrame newNackFrame(Headers headers) {
        StompControlFrame frame = new StompControlFrame( StompFrame.Command.NACK );
        frame.setHeader( StompFrame.Header.MESSAGE_ID, headers.get( StompFrame.Header.MESSAGE_ID ) );
        frame.setHeader( StompFrame.Header.SUBSCRIPTION, headers.get( StompFrame.Header.SUBSCRIPTION ) );
        String transactionId = headers.get( StompFrame.Header.TRANSACTION );
        if (transactionId != null) {
            frame.setHeader( StompFrame.Header.TRANSACTION, transactionId );
        }
        return frame;
    }

   /* public static StompFrame newSendFrame(StompMessage message) {
        StompContentFrame frame = new StompContentFrame( StompFrame.Command.SEND, message.getHeaders() );
        frame.setContent( ChannelBuffers.copiedBuffer(message.getContent()) );
        return frame;
    }*/

    public static StompFrame newConnectedFrame(String sessionId, StompFrame.Version version) {
        StompControlFrame frame = new StompControlFrame( StompFrame.Command.CONNECTED );
        frame.setHeader( StompFrame.Header.SESSION, sessionId );
        String implVersion = StompFrames.class.getPackage().getImplementationVersion();
        frame.setHeader( StompFrame.Header.SERVER, "Stilts/" + implVersion );
        if (version.isAfter( StompFrame.Version.VERSION_1_0 )) {
            frame.setHeader( StompFrame.Header.VERSION, version.versionString() );
        }
        return frame;
    }

    public static StompFrame newDisconnectFrame() {
        StompFrame frame = new StompControlFrame( StompFrame.Command.DISCONNECT );
        frame.setHeader( StompFrame.Header.RECEIPT, "connection-close" );
        return frame;
    }

    public static StompFrame newErrorFrame(String message, StompFrame inReplyTo) {
        StompContentFrame frame = new StompContentFrame( StompFrame.Command.ERROR );
        if (inReplyTo != null) {
            String receiptId = inReplyTo.getHeader( StompFrame.Header.RECEIPT );
            if (receiptId != null) {
                frame.setHeader( StompFrame.Header.RECEIPT_ID, receiptId );
            }
        }
        byte[] bytes = message.getBytes();
        frame.setContent( ChannelBuffers.copiedBuffer( bytes ) );
        frame.setHeader( StompFrame.Header.CONTENT_LENGTH, String.valueOf( bytes.length ) );
        frame.setHeader( StompFrame.Header.CONTENT_TYPE, "text/plain" );
        return frame;
    }

    public static StompFrame newReceiptFrame(String receiptId) {
        StompControlFrame receipt = new StompControlFrame( StompFrame.Command.RECEIPT );
        receipt.setHeader( StompFrame.Header.RECEIPT_ID, receiptId );
        return receipt;
    }

    // ----------------------------------------
    // ----------------------------------------

    public static StompControlFrame newBeginFrame(String transactionId) {
        StompControlFrame frame = new StompControlFrame( StompFrame.Command.BEGIN );
        frame.setHeader( StompFrame.Header.TRANSACTION, transactionId );
        return frame;
    }

    public static StompControlFrame newCommitFrame(String transactionId) {
        StompControlFrame frame = new StompControlFrame( StompFrame.Command.COMMIT );
        frame.setHeader( StompFrame.Header.TRANSACTION, transactionId );
        return frame;
    }

    public static StompControlFrame newAbortFrame(String transactionId) {
        StompControlFrame frame = new StompControlFrame( StompFrame.Command.ABORT );
        frame.setHeader( StompFrame.Header.TRANSACTION, transactionId );
        return frame;
    }

}

