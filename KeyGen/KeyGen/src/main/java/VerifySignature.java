import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.PublicKey;
import java.util.Arrays;


public class VerifySignature {
	private final static String RSA = "RSA";

	public static byte[] deSerializeByteData() {
		byte[] ret = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("encryptedbytes"));
			ret = (byte[]) in.readObject();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static PublicKey deSerializeKey() {
		PublicKey ret = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("rsapublickey"));
			ret = (PublicKey) in.readObject();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	private static byte[] decrypt(byte[] src, PublicKey uk) throws Exception {

		Cipher cipher = Cipher.getInstance(RSA);
		cipher.init(Cipher.DECRYPT_MODE, uk);
		return cipher.doFinal(src);

	}

	public static void main(String[] args) throws Exception {
		byte[] encryptedData = deSerializeByteData();
		System.out.println("Length of encrypted data(bytes): " + encryptedData.length);
		System.out.println("Encrypted data: " + Arrays.toString(encryptedData));
		PublicKey publicKey = deSerializeKey();
		byte[] decryptedData = decrypt(encryptedData, publicKey);
		System.out.println("Decrypted data: " + Arrays.toString(decryptedData));
		System.out.println("ASCII value: " + new String(decryptedData, "UTF-8"));
	}

}
