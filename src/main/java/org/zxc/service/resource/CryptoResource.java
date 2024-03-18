package org.zxc.service.resource;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.asiainfo.dacp.crypto.cipher.AesCipher;
import com.asiainfo.dacp.crypto.cipher.Base64Codec;
import com.asiainfo.dacp.crypto.cipher.DesCipher;
import com.asiainfo.dacp.crypto.cipher.Md5Crypto;

/**
 * 加密解密服务，包含md5,aes算法
 * @author david
 * 2016年9月30日
 */
@Controller
@RequestMapping("/api/crypto")
public class CryptoResource {

	/**
	 * aes加密
	 * @param data 需要加密的数据
	 * @param key 密钥
	 * @return 加密后的字符串
	 */
	@RequestMapping(value= "/aes/encrypt",method = RequestMethod.GET)
	@ResponseBody
	public String encryptAes(@RequestParam String data,@RequestParam(required=false) String key) {
		String result  = null;
		if(key == null){
			result = AesCipher.encrypt(data);
		}else{
			result = AesCipher.encrypt(data,key);
		}
		return result;
	}

	/**
	 * aes解密
	 * @param data 需要解密的数据
	 * @param key 密钥
	 * @return 解密后的字符串
	 */
	@RequestMapping(value= "/aes/decrypt",method = RequestMethod.GET)
	@ResponseBody
	public String decryptAes(@RequestParam String data,@RequestParam(required=false) String key) {
		String result  = null;
		if(key == null){
			result = AesCipher.decrypt(data);
		}else{
			result = AesCipher.decrypt(data,key);
		}
		return result;
	}

	/**
	 * md5加密服务
	 * @param data 需要加密的数据
	 * @return 加密后的数据
	 */
	@RequestMapping(value= "/md5/encrypt",method = RequestMethod.GET)
	@ResponseBody
	public String encryptMD5(@RequestParam String data) {
		return Md5Crypto.encrypt(data);
	}

	/**
	 * base64加密服务
	 * @param data 需要加密的数据
	 * @return 加密后的数据
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value= "/base64/encrypt",method = RequestMethod.GET)
	@ResponseBody
	public String encryptBase64(@RequestParam String data) throws UnsupportedEncodingException {
		byte[] b = Base64.encodeBase64(data.getBytes("utf-8"));
		return new String(b, "utf-8");
	}

	/**
	 * base64解密服务
	 * @param data 需要解密的数据
	 * @return 解密后的数据
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value= "/base64/decrypt",method = RequestMethod.GET)
	@ResponseBody
	public String decryptBase64(@RequestParam String data) throws UnsupportedEncodingException {
		byte[] b = Base64.decodeBase64(data);
		return new String(b, "utf-8");
	}

	/**
	 * DES解密
	 * @param data 需要解密的数据
	 * @param key 密钥
	 * @return 解密后的字符串
	 */
	@RequestMapping(value= "/des/decrypt",method = RequestMethod.GET)
	@ResponseBody
	public String decryptDes(@RequestParam String data,@RequestParam(required=false) String key) throws UnsupportedEncodingException {
		String result  = null;
		if(key == null){
			result = DesCipher.decrypt(data);
		}else{
			result = DesCipher.decrypt(data,key);
		}
		return result;
		// FwEncryptSecretPo po = new FwEncryptSecretPo("sys","DES","hQ+IGoGwjNHfDA9cVQ0DY2jviydppolN","KEY");

		// String result = GlobalEncryptAlgorithm.getInstanceAndDecryptKey(List.of(po)).decrypt("RSbMorT5JLH24C+0sUBSjw==");
	}
}
