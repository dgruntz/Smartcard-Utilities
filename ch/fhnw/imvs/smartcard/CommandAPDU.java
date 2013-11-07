package ch.fhnw.imvs.smartcard;

/**
 * 
 * 
 * Class representing a Command APDU created out of a bytearray. This class
 * provides methods to check an APDUs validity and to access the fields.
 * Standard and Extended Length APDUs are supported.
 * 
 * <p>
 * 
 * <b>Overview over APDUs</b>
 * 
 * <table border="1" cellspacing="0">
 * <tr>
 * <th width="10em">CLA</th>
 * <th width="10em">INS</th>
 * <th width="10em">P1</th>
 * <th width="10em">P2</th>
 * <th width="10em">Lc</th>
 * <th width="40em">Data</th>
 * <th width="10em">Le</th>
 * </tr>
 * <tr>
 * <td colspan="4" align="center">Header (required)</td>
 * <td colspan="3" align="center">Body (optional)</td>
 * </tr>
 * </table>
 * 
 * <p>
 * 
 * APDUs consist of two Parts:
 * <ul>
 * <li><b>Header:</b> 4 bytes, required.
 * <li><b>Body:</b> Variable length, optional.
 * </ul>
 * 
 * <p>
 * 
 * The header consists of four Bytes:
 * <ul>
 * <li><b>CLA:</b> Command Class.
 * <li><b>INS:</b> Instruction.
 * <li><b>P1:</b> Parameter 1
 * <li><b>P2:</b> Parameter 2
 * </ul>
 * 
 * <p>
 * 
 * The body is of variable length, depending on the APDU Case and if it's a
 * Standard or an Extended APDU. The Fields in the body have the following
 * meanings:
 * <ul>
 * <li><b>Lc:</b> Length of the Data field. Length is 1 byte for Standard APDUs
 * and 3 bytes for Extended APDUs. Default Value is 0 (No Data present)
 * <li><b>Data:</b> Application specific Argument Data. Length is stored in the
 * Lc field.
 * <li><b>Le:</b> Maximum length of answer. Length is 1 byte for Standard APDUs,
 * 2 bytes for Extended APDUs if the Lc field is present and 3 byts for Extended
 * APDUs if the Lc field is not present. Default is 0 (No answer expected). The
 * explicitly set value 0 is interpreted as 256 (For Standard APDUs) or 65536
 * (For Extended APDUs)
 * </ul>
 * 
 * <p>
 * 
 * There are four types of Command APDUs:
 * 
 * <ul>
 * <li><b>Case 1:</b> No body. (APDU Length: 4 bytes)
 * <li><b>Case 2:</b> Only the Le field is present in the body. (APDU Length: 5
 * bytes for Standard APDUs and 7 bytes for Extended APDUs)
 * <li><b>Case 3:</b> The Lc and the Data fields are present in the body. (APDU
 * Length: 5 bytes + Data Length for Standard APDUs and 7 bytes + Data Length
 * for Extended APDUs)
 * <li><b>Case 4:</b> The Lc, the Data and te Le fields are present in the body.
 * (APDU Length: 6 bytes + Data Length for Standard APDUs and 9 bytes + Data
 * Length for Extended APDUs)
 * </ul>
 * 
 * <p>
 * 
 * The Function {@link ch.fhnw.imvs.smartcard.CommandAPDU#isValidAPDU() boolean
 * isValidAPDU()} returns true only if all the rules above are followed. If the
 * APDU isn't valid, the behaviour of the class isn't specified (This includes
 * exceptions).
 * 
 * @author Christof Arnosti (christof.arnosti@fhnw.ch)
 */
public class CommandAPDU {

	/**
	 * Contains the bytearray-Representation of this APDU
	 */
	private final byte[] data;

	/**
	 * Creates a CommandAPDU object out of the raw bytes of an APDU.
	 * 
	 * @param data
	 *            The Bytearray containing the Command APDU
	 */
	public CommandAPDU(final byte[] data) {
		if (data != null) {
			this.data = new byte[data.length];
			System.arraycopy(data, 0, this.data, 0, data.length);
		} else {
			this.data = new byte[0];
		}
	}

	/**
	 * Checks if the APDU is valid. Checks for each of the case types if the
	 * length fields are set correctly and in accordance with the length of the
	 * bytearray.
	 * 
	 * @return <code>true</code> if the APDU is a correct APDU according to the
	 *         standards definition, <code>false</code> otherwise.
	 */
	public boolean isValidAPDU() {
		// Check Minimal length
		if (data.length < 4) {
			return false;
		}
		// Case 1 APDU
		else if (data.length == 4) {
			return true;
		}
		// Case 2, 3 or 4 APDU
		else {
			// Case 2 Standard APDU
			if (data.length == 5) {
				return true;
			}
			// Case 2 Extended APDU
			else if (data.length == 7 && data[4] == 0x00) {
				return true;
			}
			// Case 3 or 4 Extended APDU
			else if (data[4] == 0x00) {
				// Case 3 Extended APDU
				if (data.length == 7 + (data[5] << 8 | data[6])) {
					return true;
				}
				// Case 4 Extended APDU
				else if (data.length == 7 + (data[5] << 8 | data[6]) + 2) {
					return true;
				} else {
					return false;
				}
			}
			// Case 3 or 4 standard APDU
			else {
				// Case 3 Standard APDU
				if (data.length == 5 + data[4]) {
					return true;
				}
				// Case 4 Standard APDU
				else if (data.length == 5 + data[4] + 1) {
					return true;
				} else {
					return false;
				}
			}

		}

	}

	/**
	 * Returns whether the Data field is present in this APDU.
	 * 
	 * @return <code>true</code> if the APDU contains a Data field,
	 *         <code>false</code> otherwise.
	 */
	public boolean hasData() {
		return getLc() != 0;
	}

	/**
	 * Returns the CLA byte of the header of this APDU.
	 * 
	 * @return the Class byte of this APDU.
	 */
	public byte getCla() {
		return data[0];

	}

	/**
	 * Returns the INS byte of the header of this APDU.
	 * 
	 * @return the Instruction byte of this APDU.
	 */
	public byte getIns() {
		return data[1];
	}

	/**
	 * Returns the P1 byte of the header of this APDU.
	 * 
	 * @return the Parameter 1 byte of this APDU.
	 */
	public byte getP1() {
		return data[2];

	}

	/**
	 * Returns the P2 byte of the header of this APDU.
	 * 
	 * @return the Parameter 2 byte of this APDU.
	 */
	public byte getP2() {
		return data[3];
	}

	/**
	 * Returns the maximum number of bytes expected in the response of this
	 * APDU as encoded in the Le field.
	 * 
	 * @return the maximum namber of bytes as encoded in the Le field.
	 */
	public int getLe() {
		// Case 1 APDU
		if (data.length == 4) {
			return 0;
		}
		// Case 2 Standard APDU
		else if (!isExtendedAPDU() && data.length == 5) {
			if (data[4] == 0) {
				return 256;
			} else {
				return (data[4]);
			}
		}
		// Case 2 Extended APDU
		else if (isExtendedAPDU() && data.length == 7) {
			int res = (data[5] << 8) | data[6];
			return res == 0 ? 65536 : res;
		}
		// Case 3 APDU
		else if (data.length == 4 + (isExtendedAPDU() ? 3 : 1) + getLc()) {
			return 0;
		}
		// Case 4 Extended APDU
		else if (isExtendedAPDU()) {
			int res = (data[data.length - 2] << 8) | data[data.length - 1];
			return res == 0 ? 65536 : res;
		}
		// Case 4 Standard APDU
		else {
			int res = data[data.length - 1];
			return res == 0 ? 256 : res;
		}
	}

	/**
	 * Returns the Value of the Lc field of this APDU if present, or
	 * <code>0</code> otherwise.
	 * 
	 * @return Value of the Lc-Field or <code>0</code> if not present.
	 */
	public int getLc() {
		// Case 1 APDU
		if (data.length == 4) {
			return 0;
		}
		// Case 2 Standard APDU
		else if (!isExtendedAPDU() && data.length == 5) {
			return 0;
		}
		// Case 2 Extended APDU
		else if (isExtendedAPDU() && data.length == 7) {
			return 0;
		}
		// Case 3 / 4 Extended APDU
		else if (isExtendedAPDU()) {
			return (data[5] << 8) | data[6];
		}
		// Case 3 / 4 Standard APDU
		else {
			return data[4];
		}
	}

	/**
	 * Returns the Argument Data of this APDU.
	 * 
	 * @return <code>byte[]</code> with the length <code>getLc()</code>, filled
	 *         with the Argument Data. If no Argument Data is present, an empty
	 *         bytearray is returned.
	 */
	public byte[] getArgumentData() {
		// Case 1 / 2 APDU
		if (getLc() == 0) {
			return new byte[0];
		}
		// Case 3 / 4 Extended APDU
		else if (isExtendedAPDU()) {
			byte[] ret = new byte[getLc()];
			System.arraycopy(data, 7, ret, 0, getLc());
			return ret;
		}
		// Case 3 / 4 Standard APDU
		else {
			byte[] ret = new byte[getLc()];
			System.arraycopy(data, 5, ret, 0, getLc());
			return ret;
		}
	}

	/**
	 * Returns whether this APDU uses the extended format.
	 * 
	 * @return <code>true</code> if this APDU uses the extended length field
	 *         notation, <code>false</code> otherwise.
	 */
	public boolean isExtendedAPDU() {
		return (data.length >= 7) && data[5] == 0x00;
	}

	/**
	 * Returns a Copy of the underlying bytearray.
	 * 
	 * @return A Copy of the <code>byte[]</code>-Representation of this APDU.
	 */
	public byte[] getRaw() {
		byte[] ret = new byte[data.length];
		System.arraycopy(data, 0, ret, 0, data.length);
		return ret;
	}

}
