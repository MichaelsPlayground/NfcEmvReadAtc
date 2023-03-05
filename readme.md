# NFC EMV Read Application Transaction Counter (ATC)

This app tries to read the Application Transaction Counter (ATC) from a payment (credit) card. Unfortunately 
I only get a positive response for VisaCards but not for MasterCards. This seems to be unusual because the 
ATC is part of a cryptogram when making payments and I didn't found any information that this information 
is not available on MasterCards generally.

1) select PPSE and search for tag 0x4F in respond (available AID(s))
2) here we are selecting the AIDs manually for VisaCard (AID A0000000031010) or MasterCard (A0000000041010)
3) get the ATC: 

- response for VisaCard (hex): 0353
- response for MasterCard: 6a88 -> this is an error code meaning "Referenced data not found"

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

AndroidManifest.xml:
```plaintext
    <!-- needed for NFC -->
    <uses-permission android:name="android.permission.NFC" />
    <uses-permission android:name="android.permission.VIBRATE" />
```