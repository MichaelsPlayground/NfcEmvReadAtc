# NFC EMV Read Application Transaction Counter (ATC)

This app tries to read the Application Transaction Counter (ATC) from a payment (credit) card. Unfortunately 
I get a positive response only for VisaCards but not for MasterCards. 

This seems to be unusual because the ATC is part of a cryptogram when making payments and I didn't found any 
information that this information is not available on MasterCards generally.

1) select PPSE and search for tag 0x4F in respond (available AID(s))
2) here we are selecting the AIDs manually for VisaCard (AID A0000000031010) or MasterCard (A0000000041010)
3) get the ATC: 

- response for VisaCard (hex): 0353
- response for MasterCard: 6a88 -> this is an error code meaning "Referenced data not found"

This project is the basis for a question on StackOverflow: https://stackoverflow.com/questions/75643247/emv-get-application-transaction-counter-fails-for-mastercard-but-not-for-visacar

Workflow for a VisaCard
```plaintext
01 select PPSE
01 select PPSE command length 20 data:  00a404000e325041592e5359532e444446303100
01 select PPSE response length 47 data: 6f2b840e325041592e5359532e4444463031a519bf0c1661144f07a00000000310109f0a0800010501000000009000

02 select a VisaCard
02 select a VisaCard command length 13 data: 00a4040007a000000003101000
02 select a VisaCard response length 97 data: 6f5d8407a0000000031010a5525010564953412044454249542020202020208701029f38189f66049f02069f03069f1a0295055f2a029a039c019f37045f2d02656ebf0c1a9f5a0531082608269f0a080001050100000000bf6304df2001809000

02 select a MasterCard
02 select a MasterCard command length 13 data: 00a4040007a000000004101000
02 select a MasterCard response length 2 data: 6a82

03 getApplicationTransactionCounter
applicationTransactionCounter (hex): 0353
```

Workflow for a MasterCard
```plaintext
01 select PPSE
01 select PPSE command length 20 data:  00a404000e325041592e5359532e444446303100
01 select PPSE response length 64 data: 6f3c840e325041592e5359532e4444463031a52abf0c2761254f07a000000004101050104465626974204d6173746572436172648701019f0a04000101019000

02 select a VisaCard
02 select a VisaCard command length 13 data: 00a4040007a000000003101000
02 select a VisaCard response length 2 data: 6a82

02 select a MasterCard
02 select a MasterCard command length 13 data: 00a4040007a000000004101000
02 select a MasterCard response length 86 data: 6f528407a0000000041010a54750104465626974204d6173746572436172649f12104465626974204d6173746572436172648701019f1101015f2d046465656ebf0c119f0a04000101019f6e07028000003030009000

03 getApplicationTransactionCounter
applicationTransactionCounter (hex): 6a88
```


code used to read the Application Transaction Counter:
```plaintext
    private byte[] getApplicationTransactionCounter(IsoDep nfc) {
        byte[] cmd = new byte[]{(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x36, (byte) 0x00};
        byte[] result = new byte[0];
        try {
            result = nfc.transceive(cmd);
        } catch (IOException e) {
            System.out.println("* getApplicationTransactionCounter failed");
            return null;
        }
        if (result.length == 7) {
            // example result: 9f360200169000, counter is (hex) 00 16 = (decimal) 22
            return Arrays.copyOfRange(result, (result.length - 4), (result.length - 2));
        }
        return result;
    }
```

Don't forget to append these permissions:

AndroidManifest.xml:
```plaintext
    <!-- needed for NFC -->
    <uses-permission android:name="android.permission.NFC" />
    <uses-permission android:name="android.permission.VIBRATE" />
```
