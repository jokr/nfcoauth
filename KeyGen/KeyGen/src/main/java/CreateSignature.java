import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class CreateSignature {
	public static void main(String[] args) throws Exception {
		KeyPair keyPair = genKeyPair();

		X509EncodedKeySpec publicKey = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
		PKCS8EncodedKeySpec privateKey = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());

		Files.write(Paths.get("privatekey"), privateKey.getEncoded());
		Files.write(Paths.get("publickey"), publicKey.getEncoded());

		byte[] data = Files.readAllBytes(Paths.get("data"));
		byte[] digest = digest(data, 5);
		byte[] signature = sign(keyPair.getPrivate(), digest);
		Files.write(Paths.get("data.signed"), signature);

		byte[] payload = new byte[data.length + signature.length];
		System.arraycopy(data, 0, payload, 0, data.length);
		System.arraycopy(signature, 0, payload, data.length, signature.length);

		System.out.println(String.format("Data: %d bytes", data.length));
		System.out.println(String.format("Digest: %d bytes", digest.length));
		System.out.println(String.format("Signature: %d bytes", signature.length));
		System.out.println(String.format("Payload: %d bytes", payload.length));
	}

	private static KeyPair genKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException {
		Security.addProvider(new BouncyCastleProvider());
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", "BC");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		gen.initialize(128, random);
		return gen.generateKeyPair();
	}

	private static byte[] digest(byte[] plainText, int length) throws NoSuchAlgorithmException {
		MessageDigest m = MessageDigest.getInstance("SHA-256");
		m.update(plainText);
		if (length == 0) {
			return m.digest();
		}
		byte[] digest = new byte[length];
		System.arraycopy(m.digest(), 0, digest, 0, digest.length);
		return digest;
	}

	private static byte[] sign(PrivateKey key, byte[] digest) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, SignatureException {
		Signature signature = Signature.getInstance("NONEwithRSA");
		signature.initSign(key);
		signature.update(digest);
		return signature.sign();
	}
}
