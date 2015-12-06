import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;

public class CreateSignature {
	public static void main(String[] args) throws Exception {
		KeyPair keyPair = genKeyPair();
		Files.write(Paths.get("key"), keyPair.getPrivate().getEncoded());
		Files.write(Paths.get("key.pub"), keyPair.getPublic().getEncoded());

		byte[] data = Files.readAllBytes(Paths.get("data"));
		System.out.println(new String(data, "UTF-8"));
		byte[] signature = sign(keyPair.getPrivate(), data);
		System.out.println(signature.length);
		System.out.println(new BigInteger(130, new SecureRandom()).toString(32));
	}

	private static KeyPair genKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException {
		Security.addProvider(new BouncyCastleProvider());
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", "BC");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		gen.initialize(256, random);
		return gen.generateKeyPair();
	}

	private static byte[] sign(PrivateKey key, byte[] plainText) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, SignatureException {
		System.out.println(plainText.length);
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.update(plainText);
		byte[] digest = m.digest();
		System.out.println(digest.length);
		Signature signature = Signature.getInstance("NONEwithRSA");
		signature.initSign(key);
		signature.update(digest);
		return signature.sign();
	}
}
