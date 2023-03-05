package de.androidcrypto.nfcemvreadatc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback{

    com.google.android.material.textfield.TextInputEditText etLog;
    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etLog = findViewById(R.id.etLog);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        IsoDep nfc = null;
        nfc = IsoDep.get(tag);
        if (nfc != null) {
            // vibrate the device
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150, 10));
            } else {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
            runOnUiThread(() -> {
                etLog.setText("");
            });
            try {
                nfc.connect();

                // step 01 select the PPSE
                writeToUiAppend(etLog, "");
                writeToUiAppend(etLog, "01 select PPSE");
                byte[] command;
                byte[] PPSE = "2PAY.SYS.DDF01".getBytes(StandardCharsets.UTF_8);
                command = selectApdu(PPSE);
                byte[] responsePpse = new byte[0];
                responsePpse = nfc.transceive(command);
                writeToUiAppend(etLog, "01 select PPSE command length " + command.length + " data: " + bytesToHex(command));
                writeToUiAppend(etLog, "01 select PPSE response length " + responsePpse.length + " data: " + bytesToHex(responsePpse));

                // first selecting a VisaCard
                byte[] visaCard = hexToBytes("A0000000031010");
                byte[] responseVisaCard = new byte[0];
                command = selectApdu(visaCard);
                responseVisaCard = nfc.transceive(command);
                writeToUiAppend(etLog, "");
                writeToUiAppend(etLog, "02 select a VisaCard");
                writeToUiAppend(etLog, "02 select a VisaCard command length " + command.length + " data: " + bytesToHex(command));
                writeToUiAppend(etLog, "02 select a VisaCard response length " + responseVisaCard.length + " data: " + bytesToHex(responseVisaCard));

                // second selecting a MasterCard
                byte[] masterCard = hexToBytes("A0000000041010");
                byte[] responseMasterCard = new byte[0];
                command = selectApdu(masterCard);
                responseMasterCard = nfc.transceive(command);
                writeToUiAppend(etLog, "");
                writeToUiAppend(etLog, "02 select a MasterCard");
                writeToUiAppend(etLog, "02 select a MasterCard command length " + command.length + " data: " + bytesToHex(command));
                writeToUiAppend(etLog, "02 select a MasterCard response length " + responseMasterCard.length + " data: " + bytesToHex(responseMasterCard));

                writeToUiAppend(etLog, "");
                writeToUiAppend(etLog, "03 getApplicationTransactionCounter");
                byte[] applicationTransactionCounter = getApplicationTransactionCounter(nfc);
                if (applicationTransactionCounter != null) {
                    writeToUiAppend(etLog, "applicationTransactionCounter (hex): " + bytesToHex(applicationTransactionCounter));
                } else {
                    writeToUiAppend(etLog, "applicationTransactionCounter (hex): NULL");
                }

            } catch (IOException e) {
                writeToUiAppend(etLog, "Error: " + e.getMessage());
            }



        }
    }

    private byte[] selectApdu(byte[] aid) {
        byte[] commandApdu = new byte[6 + aid.length];
        commandApdu[0] = (byte) 0x00;  // CLA
        commandApdu[1] = (byte) 0xA4;  // INS
        commandApdu[2] = (byte) 0x04;  // P1
        commandApdu[3] = (byte) 0x00;  // P2
        commandApdu[4] = (byte) (aid.length & 0x0FF);       // Lc
        System.arraycopy(aid, 0, commandApdu, 5, aid.length);
        commandApdu[commandApdu.length - 1] = (byte) 0x00;  // Le
        return commandApdu;
    }

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

    /**
     * converts a byte array to a hex encoded string
     * @param bytes
     * @return hex encoded string
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    /**
     * converts a hex encoded string to a byte array
     * @param str
     * @return
     */
    public static byte[] hexToBytes(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2),
                    16);
        }
        return bytes;
    }

    private void writeToUiAppend(TextView textView, String message) {
        runOnUiThread(() -> {
            if (TextUtils.isEmpty(textView.getText().toString())) {
                textView.setText(message);
            } else {
                String newString = textView.getText().toString() + "\n" + message;
                textView.setText(newString);
            }
        });
        System.out.println(message);
    }

    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcAdapter != null) {

            if (!mNfcAdapter.isEnabled())
                showWirelessSettings();

            Bundle options = new Bundle();
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);

            // Enable ReaderMode for all types of card and disable platform sounds
            // the option NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK is NOT set
            // to get the data of the tag afer reading
            mNfcAdapter.enableReaderMode(this,
                    this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F |
                            NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NFC_BARCODE |
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    options);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableReaderMode(this);
    }

}