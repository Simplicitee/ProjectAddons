package me.simplicitee.project.addons.util;

import java.util.Arrays;
import java.util.Random;

/**
 * Class for defining hexadecimal color codes, and translating them from RGB
 * @author simp
 *
 */
public class HexColor {
	
	protected static final char[] VALUES = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	protected static final HexColor[] RGB = {HexColor.RED, HexColor.GREEN, HexColor.BLUE};
	protected static final HexColor[] CMY = {HexColor.CYAN, HexColor.MAGENTA, HexColor.YELLOW};
	protected static final HexColor[] DEFAULTS = {HexColor.RED, HexColor.GREEN, HexColor.BLUE, HexColor.CYAN, HexColor.MAGENTA, HexColor.YELLOW, HexColor.WHITE, HexColor.BLACK};
	
	public static final HexColor RED = new HexColor("ff0000");
	public static final HexColor GREEN = new HexColor("00ff00");
	public static final HexColor BLUE = new HexColor("0000ff");
	public static final HexColor WHITE = new HexColor("ffffff");
	public static final HexColor BLACK = new HexColor();
	public static final HexColor CYAN = HexColor.BLUE.add(HexColor.GREEN);
	public static final HexColor MAGENTA = HexColor.RED.add(HexColor.BLUE);
	public static final HexColor YELLOW = HexColor.RED.add(HexColor.GREEN);
	
	private String hexcode;
	
	/**
	 * Creates an instance of {@link HexColor} with the hex code 0x000000 (black)
	 */
	public HexColor() {
		this("000000");
	}
	
	/**
	 * Creates an instance of {@link HexColor} with the specified rgb values
	 * @param r red amount
	 * @param g green amount
	 * @param b blue amount
	 */
	public HexColor(int r, int g, int b) {
		this.hexcode = fromRGB(r, g, b);
	}
	
	/**
	 * Creates an instance of {@link HexColor} with the specified hexcode. 
	 * <br><br><b>If</b> the string contains a digit not accepted by hexadecimal (that is, a-f and 0-9), the entire string is defaulted to black (000000)
	 * @param hexcode color hexcode in the form of 6 digits (e.g; black is 000000)
	 */
	public HexColor(String hexcode) {
		hexcode = hexcode.toLowerCase();
		
		if (hexcode.startsWith("#")) {
			hexcode = hexcode.substring(1, hexcode.length());
		}
		
		if (!hexcode.matches("[0-9a-f]{6}")) {
			hexcode = "000000";
		}
		
		this.hexcode = hexcode;
	}
	
	/**
	 * Creates an instance of {@link HexColor} with a pseudorandom rgb value using the rand param
	 * @param rand Random to determine pseudorandom rgb
	 */
	public HexColor(Random rand) {
		this(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
	}
	
	/**
	 * Returns a hexcode string for a {@link HexColor} from rgb values
	 * @param r red amount
	 * @param g green amount
	 * @param b blue amount
	 * @return hex hexcode string from rgb values
	 */
	private String fromRGB(int r, int g, int b) {
		if (r < 0) {
			r = 0;
		} else if (r > 255) {
			r = 255;
		}
		
		if (g < 0) {
			g = 0;
		} else if (g > 255) {
			g = 255;
		}
		
		if (b < 0) {
			b = 0;
		} else if (b > 255) {
			b = 255;
		}
		
		int[] rgb = {r, g, b};
		StringBuilder hex = new StringBuilder();
		
		for (int i = 0; i < 3; i++) {
			int first = (int) Math.floor(rgb[i]/16);
			int second = rgb[i]%16;
			
			hex.append(VALUES[first]);
			hex.append(VALUES[second]);
		}
		
		return hex.toString();
	}
	
	/**
	 * Adds the color param to the current {@link HexColor} instance, creating a new {@link HexColor} instance
	 * @param color {@link HexColor} to add to the current {@link HexColor}
	 * @return new {@link HexColor} instance combining the two previous {@link HexColor}
	 */
	public HexColor add(HexColor color) {
		int[] a = this.toRGB();
		int[] b = color.toRGB();
		int[] outcome = {0, 0, 0};
		
		for (int i = 0; i < 3; i++) {
			int newVal = Math.min(Math.max(0, a[i] + b[i]), 255);
			outcome[i] = newVal;
		}
		
		return new HexColor(outcome[0], outcome[1], outcome[2]);
	}
	
	/**
	 * Substracts the color param from the current {@link HexColor} instance, creating a new HexColor instance
	 * @param color {@link HexColor} to subtract from the current {@link HexColor}
	 * @return new {@link HexColor} instance 
	 */
	public HexColor subtract(HexColor color) {
		int[] a = this.toRGB();
		int[] b = color.toRGB();
		int[] outcome = {0, 0, 0};
		
		for (int i = 0; i < 3; i++) {
			int newVal = Math.min(Math.max(0, a[i] - b[i]), 255);
			outcome[i] = newVal;
		}
		
		return new HexColor(outcome[0], outcome[1], outcome[2]);
	}
	
	/**
	 * Converts the {@link HexColor} into an array of rgb values
	 * @return rgb values
	 */
	public int[] toRGB() {
		String red = hexcode.substring(0, 2);
		String green = hexcode.substring(2, 4);
		String blue = hexcode.substring(4, 6);
		
		char[] reds = red.toCharArray();
		char[] greens = green.toCharArray();
		char[] blues = blue.toCharArray();
		
		int redVal = Arrays.binarySearch(VALUES, reds[0])*16 + Arrays.binarySearch(VALUES, reds[1]);
		int greenVal = Arrays.binarySearch(VALUES, greens[0])*16 + Arrays.binarySearch(VALUES, greens[1]);
		int blueVal = Arrays.binarySearch(VALUES, blues[0])*16 + Arrays.binarySearch(VALUES, blues[1]);
		
		return new int[] {redVal, greenVal, blueVal};
	}
	
	/**
	 * Checks if the current {@link HexColor} matches the color param
	 * @param color {@link HexColor} to check against
	 * @return true if hexcodes match
	 */
	public boolean matches(HexColor color) {
		return hexcode.equalsIgnoreCase(color.getHexcode());
	}
	
	/**
	 * Gets the string storing the hexcode, in the format of 6 hexadecimal digits (e.g; black is 000000)
	 * @return hexcode string
	 */
	public String getHexcode() {
		return hexcode;
	}
	
	/**
	 * An array of default {@link HexColor}
	 * @return array of default {@link HexColor}
	 */
	public static HexColor[] values() {
		return DEFAULTS;
	}
	
	/**
	 * An array of red, green, and blue {@link HexColor} instances
	 * @return array of rgb {@link HexColor}
	 */
	public static HexColor[] rgb() {
		return RGB;
	}
	
	/**
	 * An array of cyan, magenta, and yellow {@link HexColor} instances
	 * @return array of cmy {@link HexColor}
	 */
	public static HexColor[] cmy() {
		return CMY;
	}
}
