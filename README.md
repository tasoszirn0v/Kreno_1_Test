# Kreno_1_Test

IM app (10/05/2022)
This a project for Instant Messaging (IM) app created for academic purposes and is focused in front-end design & development using Android Studio and Kotlin.

The back-end server is executed by Firebase (Authentication by phone number+SMS with verification code (OTP), database Realtime DB, cloud storage for user created content, FCM for push notifications).

The project is connected to my master thesis about encrypted IM apps focusing on E2EE (esp. Signal protocol & app).

Starting from scratch, testing cryptographic primitives (algorithms and hash functions), crypto libraries and various implementantions...

Initially, creating a TOFU scheme for testing code and layout using Firebase uid, sha2-256.

Then, implementing a simple symmetric cipher...AES/CBC/PKCS5Padding with a random 256-bit key and a random 128-bit IV.
The test of the symmetric primitive includes the key and the iv being uploaded in Realtime DB and Alice & Bob retrieve them iot to encrypt/decrypt their messages.

At this point, the app is implementing a simple E2EE protocol(not checked/verified!!!) using Spongy Castle:
1. Creating EC key pair (NIST P-256)
2. Store private key in Keystore (now for testing and checks is stored in SharedPreferences)
3. Upload public key in Realtime DB
4. Key exchange of public keys and ECDH key agreement for common shared secret
5. Sha-2 of common secret and keep 32 bytes as AES 256-bit key 
6. Use cipher AES/GCM/NoPadding (message+iv (96-bit)+key) and get ciphertext+authentication tag (128-bit) for encryption - Alice
7. Decryption: Bob receives ciphertext+tag and using the cipher and the key, is able to get the message...
