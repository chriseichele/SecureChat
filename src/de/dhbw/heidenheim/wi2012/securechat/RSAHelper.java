package de.dhbw.heidenheim.wi2012.securechat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
//import java.security.KeyPair;
//import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.util.Base64;


/**
 *
 * @author VeWag
 */
public class RSAHelper {

	/*

   public static void main(String[] args) throws Exception {
	   RSAHelper helper = new RSAHelper();

       KeyPair keyPair1 = helper.generateKeyPair();
       KeyPair keyPair2 = helper.generateKeyPair();
       String Text = "abcdefghijklmnopqrstuvwxyz";

       byte[] data = Text.getBytes("UTF-8");
       byte[] digitalSignature = helper.signData(data, keyPair1.getPrivate());       

       String Signatur = new String(Base64.encode(digitalSignature, Base64.DEFAULT));
       //System.out.println("Signatur " + Signatur);

       String VerschlüsselterText = helper.encrypt(Text, keyPair2.getPublic());

 		//__________________________ Übertragung _____________________________________

       String EntschlüsselterKlartext = helper.decrypt(VerschlüsselterText, keyPair2.getPrivate()); 
       byte[] ByteEntschlüsselterKlartext = EntschlüsselterKlartext.getBytes("UTF-8");


       byte[] ByteSignature = Base64.decode(Signatur, Base64.DEFAULT);

       //Signatur echt?
       boolean echt;
       echt = helper.verifySig(ByteEntschlüsselterKlartext, keyPair1.getPublic(), ByteSignature);   //Klartext, getPublic, Signature
       System.out.println("echt ? " + echt);

   }

	 */

	public byte[] signData(byte[] data, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature signer = Signature.getInstance("MD5WithRSA");
		signer.initSign(key);
		signer.update(data);
		return (signer.sign());
	}

	public boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature signer = Signature.getInstance("MD5WithRSA");
		signer.initVerify(key);
		signer.update(data);
		return signer.verify(sig);
	}

	/*
 public KeyPair generateKeyPair() throws Exception {
   KeyPair rsaKeys = KeyPairGenerator.getInstance("RSA").genKeyPair();
   return rsaKeys;
 }
	 */

	public String encrypt(String Klartext, PublicKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException { 

		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] BytesKlartext = cipher.doFinal(Klartext.getBytes());

		String verschluesselterText = new String(Base64.encode(BytesKlartext, Base64.DEFAULT));

		return verschluesselterText;
	}

	public String decrypt(String Geheimtext, PrivateKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		byte[] ByteGeheimtext = Base64.decode(Geheimtext, Base64.DEFAULT);

		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);

		byte[] ByteKlartext = cipher.doFinal(ByteGeheimtext);
		String Klartext = new String (ByteKlartext);

		return Klartext;
	}

	// Methoden um Schlüssel umzuwandeln

	public static String getPrivateKeyString(PrivateKey k) {
		return Base64.encodeToString(k.getEncoded(), Base64.DEFAULT);
	}
	public static String getPublicKeyString(PublicKey k) {
		return Base64.encodeToString(k.getEncoded(), Base64.DEFAULT);
	}

	public static PrivateKey getPrivateKey(String ks) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = Base64.decode(ks.getBytes("utf-8"), 0);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory fact = KeyFactory.getInstance("RSA");
		PrivateKey priv = fact.generatePrivate(keySpec);
		return priv;
	}
	public static PublicKey getPublicKey(String ks) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
		byte[] keyBytes = Base64.decode(ks.getBytes("utf-8"), 0);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey key = keyFactory.generatePublic(spec);
		return key;
	}

}