package Eup_JTT808_Server;

import java.util.Arrays;

public class JTT808Message {	
	
	private static final int MINIMUM_MESSAGE_LEN = 3;
	
	private static final byte SPBYTE_HEAD = 0x7e; // identification byte
	private static final byte SPBYTE_TAIL = 0x02; 
	private static final byte ESBYTE_HEAD = 0x7d; // escape byte
	private static final byte ESBYTE_TAIL = 0x01;
	
	public static byte[] encode_message (byte[] raw_byte_list)
	{
		try {
			int len = 0;
			byte[] tmp_byte_list = new byte[raw_byte_list.length * 2];
			tmp_byte_list[len++] = SPBYTE_HEAD;
			for (int i = 0; i < raw_byte_list.length; i++) {
				byte b = raw_byte_list[i];
				if (b == SPBYTE_HEAD) {
					tmp_byte_list[len++] = ESBYTE_HEAD;
					tmp_byte_list[len++] = SPBYTE_TAIL;
				} else if (b == ESBYTE_HEAD) {
					tmp_byte_list[len++] = b;
					tmp_byte_list[len++] = ESBYTE_TAIL;
				} else {
					tmp_byte_list[len++] = b;
				}
			}
			tmp_byte_list[len++] = SPBYTE_HEAD;
			byte[] enc_byte_list = new byte[len];
			System.arraycopy(tmp_byte_list, 0, enc_byte_list, 0, len);
			return enc_byte_list;
		} catch (Exception e) {
			throw e;
		}
	}	
	
	public static byte[] decode_message (byte[] enc_byte_list)
	{			
		try {
			if (enc_byte_list.length < MINIMUM_MESSAGE_LEN || 
			    enc_byte_list[0] != SPBYTE_HEAD || 
			    enc_byte_list[enc_byte_list.length - 1] != SPBYTE_HEAD) {
				// invalid
				return null;
			}
			int len = 0;
			boolean is_format_error = false;
			byte[] tmp_byte_list = new byte[enc_byte_list.length];
			for (int i = 1; i < enc_byte_list.length - 2; i++) {
				byte b = enc_byte_list[i];
				byte c = enc_byte_list[i + 1];
				if (b == ESBYTE_HEAD) {
					if (c == SPBYTE_TAIL) {
						tmp_byte_list[len++] = SPBYTE_HEAD;
					} else if (c == ESBYTE_TAIL) {
						tmp_byte_list[len++] = ESBYTE_HEAD;
					} else {
						is_format_error = true;
						break;
					}
					i++;
				} else {
					tmp_byte_list[len++] = b;
				}
			}
			if (is_format_error) {
				return null;
			}
			byte[] dec_byte_list = new byte[len];
			System.arraycopy(tmp_byte_list, 0, dec_byte_list, 0, len);
			return dec_byte_list;
		} catch (Exception e) {
			throw e;
		}				
	}
	
	public static void unit_test () {
		String test_string = "}avr34}5679~";		
		byte[] correct_enc_list = {0x7e, 0x7d, 0x01, 0x61, 0x76, 0x72, 0x33, 0x34, 0x7d, 0x01, 0x35, 0x36, 0x37, 0x39, 0x7d, 0x02, 0x7e};
		byte[] correct_dec_list = {0x7d, 0x61, 0x76, 0x72, 0x33, 0x34, 0x7d, 0x35, 0x36, 0x37, 0x39, 0x7e};		
		byte[] enc_list = JTT808Message.encode_message(test_string.getBytes());		
		System.out.println(String.format("jtt808 encode api test : %b", Arrays.equals(enc_list, correct_enc_list)));
		byte[] dec_list = JTT808Message.decode_message(enc_list);		
		System.out.println(String.format("jtt808 decode api test : %b", Arrays.equals(dec_list, correct_dec_list)));
	}
	
	public static byte[] create_message (byte[] message_body) {
		byte[] data = null;
		try {
			byte body_version = 0;
			boolean has_pack = false;
			int encrypt_method = -1;
			int message_body_length = -1;			 
			byte[] message_body_attr = create_message_body_attribute(body_version, (byte)(has_pack ? 1 : 0), encrypt_method, message_body_length);
			int message_id = -1;
			int protocol_version = -1; 
			byte[] phone_number = new byte[10];
			int message_serial_id = -1;			
			int pack_number = -1;
			int pack_serial = -1;
			byte[] message_head = create_message_header(message_id, message_body_attr, protocol_version, 
					phone_number, message_serial_id, has_pack, pack_number, pack_serial);
			byte checksum = create_checksum(message_head, message_body);
			return data;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static byte[] create_message_header (
			int message_id, 
			byte[] message_body_attr, 
			int protocol_version, 
			byte[] phone_number, 
			int message_serial_id, 
			boolean has_pack, 
			int pack_number, 
			int pack_serial)
	{
		byte[] data = null;
		try {	
			if (has_pack) {
				data = new byte[20];
			} else {
				data = new byte[16];
			}
			data[0] = (byte)(message_id >> 8);
			data[1] = (byte)(message_id & 0xFF);
			data[2] = message_body_attr[0];
			data[3] = message_body_attr[1];
			data[4] = (byte)(protocol_version);
			System.arraycopy(phone_number, 0, data, 5, phone_number.length);
			data[15] = (byte)(message_serial_id >>> 8);
			data[16] = (byte)(message_serial_id);
			if (has_pack) {
				data[17] = (byte)(pack_number >> 8);
				data[18] = (byte)(pack_number & 0xFF);
				data[19] = (byte)(pack_serial >> 8);
				data[20] = (byte)(pack_serial & 0xFF);
			}
			return data;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static byte[] create_message_body_attribute (byte body_version, byte has_pack, int encrypt_method, int message_body_length)
	{
		byte[] data = new byte[2];
		try {
			data[0] = (byte)((body_version << 7) | (has_pack << 6) | (encrypt_method << 2) | (message_body_length >> 8));
			data[1] = (byte)(message_body_length & 0xFF);
			return data;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static byte create_checksum (byte[] message_head, byte[] message_body)
	{
		byte checksum = 0;
		for (int i = 0; i < message_head.length; i++)
		{
			checksum ^= message_head[i];
		}
		for (int i = 0; i < message_body.length; i++)
		{
			checksum ^= message_body[i];
		}
		return checksum;
	}
	
	public static byte[] create_dev_response (int server_serial_id, int server_message_id, byte result) {
		byte[] data = new byte[7];		
		try {
			int message_id = JTT808MessageID.DVR_RESPONSE;			
			data[0] = (byte)(server_serial_id >> 8);
			data[1] = (byte)(server_serial_id & 0xFF);
			data[2] = (byte)(server_message_id >> 8);
			data[3] = (byte)(server_message_id & 0xFF);
			data[4] = (byte)(result);
			return data;			
		} catch (Exception e) {
			throw e;
		}		
	}
}