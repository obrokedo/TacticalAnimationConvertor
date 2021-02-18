package mb.utils.dumper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import mb.fc.utils.gif.GifDecoder;

public class DumperApp {
	public static void main(String args[]) {
		if (args.length != 1 && args.length != 2) {
			System.out.println("Incorrect arguments provided: Expects 'directory-to-convert [output-directory]'");
			System.exit(1);
		}
		DumperApp da = new DumperApp();
		da.convertDir(args[0], (args.length == 2 ? args[1]: null));
	}

	// Hero path, output path
	// Spell path, output path
	public void convertDir(String path, String outputPath) {
		File dir = new File(path);
		if (outputPath != null) {
			File outDir = new File(outputPath);
			if (!outDir.exists()) {
				outDir.mkdirs();
			}
		}
		for (String file : dir.list()) {
			System.out.println("Processing: " + file);
			if (file.endsWith(".gif")) {
				if (file.contains("walk")) {
					convertWalkGif(file, path, outputPath);
				} else {
					convertAnimGif(file, path, outputPath);
				}
			}
		}
	}

	public void convertAnimGif(String gif, String path, String outputPath) {
		GifDecoder decoder = new GifDecoder();
		decoder.read(path + "/" + gif);
		try {
			generateSpriteSheet(decoder, gif, "", new Color(decoder.getImage().getRGB(0, 0)), outputPath);
			
			ArrayList<String> animStrings = new ArrayList<>();
			addCombatAnimations(decoder, 0, 0, gif.replace(".gif", ""), "", animStrings);
			exportAnimations(animStrings, gif, outputPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void convertWalkGif(String gif, String path, String outputPath) {
		GifDecoder decoder = new GifDecoder();
		decoder.read(path + "/" + gif);
		try {
			generateSpriteSheet(decoder, gif, "", null, outputPath);
			ArrayList<String> animStrings = new ArrayList<>();
			addWalkAnimations(animStrings, "", "");
			exportAnimations(animStrings, gif, outputPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void exportAnimations(ArrayList<String> animStrings, String imageName, String outputPath) throws IOException {
		animStrings.add(0, "<animations spriteSheet=\""
				+ imageName.substring(imageName.lastIndexOf(File.separator) + 1).replace(".gif", ".sprites")
				+ "\" ver=\"1.2\">");
		animStrings.add("</animations>");
		imageName = (outputPath != null ? outputPath + "/" : "") + imageName;
		Path animPath = Paths.get(imageName.replace(".gif", ".anim"));
		Files.write(animPath, animStrings, StandardCharsets.UTF_8);
		
		System.out.println("Animation successfully written to " + animPath.toString());
	}
	
	private static void addWalkAnimations(ArrayList<String> anims, String directory, String prefix) {
	    anims.add("<anim name=\"" + prefix + "Up\" loops=\"0\">");
	    anims.add("<cell index=\"0\" delay=\"1\">");
	    anims.add("<spr name=\"/" + directory + "Frame4\" x=\"0\" y=\"0\" z=\"0\"/>");
	    anims.add("</cell>");
	    anims.add("<cell index=\"1\" delay=\"1\">");
	    anims.add("<spr name=\"/" + directory + "Frame5\" x=\"0\" y=\"0\" z=\"0\"/>");
	    anims.add("</cell>");
	    anims.add("</anim>");
	    anims.add("<anim name=\"" + prefix + "Down\" loops=\"0\">");
	    anims.add("<cell index=\"0\" delay=\"1\">");
	    anims.add("<spr name=\"/" + directory + "Frame0\" x=\"0\" y=\"0\" z=\"0\"/>");
	    anims.add("</cell>");
	    anims.add("<cell index=\"1\" delay=\"1\">");
	    anims.add("<spr name=\"/" + directory + "Frame1\" x=\"0\" y=\"0\" z=\"0\"/>");
	    anims.add("</cell>");
	    anims.add("</anim>");
	    anims.add("<anim name=\"" + prefix + "Left\" loops=\"0\">");
	    anims.add("<cell index=\"0\" delay=\"1\">");
	    anims.add("<spr name=\"/" + directory + "Frame2\" x=\"0\" y=\"0\" z=\"0\" flipH=\"1\"/>");
	    anims.add("</cell>");
	    anims.add("<cell index=\"1\" delay=\"1\">");
	    anims.add("<spr name=\"/" + directory + "Frame3\" x=\"0\" y=\"0\" z=\"0\" flipH=\"1\"/>");
	    anims.add("</cell>");
	    anims.add("</anim>");
	    anims.add("<anim name=\"" + prefix + "Right\" loops=\"0\">");
	    anims.add("<cell index=\"0\" delay=\"1\">");
	    anims.add("<spr name=\"/" + directory + "Frame2\" x=\"0\" y=\"0\" z=\"0\"/>");
	    anims.add("</cell>");
	    anims.add("<cell index=\"1\" delay=\"1\">");
	    anims.add("<spr name=\"/" + directory + "Frame3\" x=\"0\" y=\"0\" z=\"0\"/>");
	    anims.add("</cell>");
	    anims.add("</anim>");
	  }
	
	private static void addCombatAnimations(GifDecoder gifDecoder,
		int xOffset, int yOffset, String animName, String directory, ArrayList<String> animStrings) {
      	animStrings.add("<anim name=\"" + animName + "\" loops=\"0\">");
		int index = 0;			
		while (true) {
			animStrings.add("<cell index=\"" + index + "\" delay=\"" + gifDecoder.getDelay(index) + "\">");
	        animStrings.add(
	            "<spr name=\"/" + directory + "Frame" + index + "\" x=\"" + xOffset + 
	            	"\" y=\"" + yOffset + "\" z=\"0\"/>");
			animStrings.add("</cell>");

			index++;

			if (index == gifDecoder.getFrameCount())
				break;
		}

		animStrings.add("</anim>");
	}

	public static void generateSpriteSheet(GifDecoder gifDecoder, String imageName,
			String directory, Color trans, String outputPath) throws IOException {
		Dimension battleImageDims = null;
			battleImageDims = new Dimension(gifDecoder.getFrameCount() * (gifDecoder.getFrameSize()).width,
					(int) gifDecoder.getFrameSize().getHeight());

		BufferedImage bim = new BufferedImage(
				battleImageDims.width,
				battleImageDims.height,
				BufferedImage.TYPE_INT_ARGB);

		ArrayList<String> spriteSheetContents = new ArrayList<>();
		spriteSheetContents.add(
				"<img name=\"" + imageName.substring(imageName.lastIndexOf(File.separator) + 1).replace(".gif", ".png")
						+ "\" w=\"" + bim.getWidth() + "\" h=\"" + bim.getHeight() + "\">\n");
		spriteSheetContents.add("\t<definitions>\n");
		spriteSheetContents.add("\t\t<dir name=\"/\">\n");
		// Only add this directory if there is actually something to add. This will be 0
		// length for spells and npcs
		if (directory.length() > 0) {
			spriteSheetContents.add("\t\t\t<dir name=\"" + directory.substring(0, directory.length() - 1) + "\">\n");
		}
		Graphics g = bim.createGraphics();

		// Add battle animations
		if (gifDecoder != null) {
			for (int i = 0; i < gifDecoder.getFrameCount(); i++) {
				spriteSheetContents.add("\t\t\t\t<spr name=\"Frame" + i + "\" x=\"" + i * gifDecoder.getFrameSize().width
								+ "\" y=\"0\" w=\"" + gifDecoder.getFrameSize().width + "\" h=\""
								+ gifDecoder.getFrameSize().height + "\"/>\n");
				if (trans != null)
					g.drawImage(transformColorToTransparency(gifDecoder.getFrame(i), trans), i * gifDecoder.getFrameSize().width, 0, null);
				else
					g.drawImage(gifDecoder.getFrame(i), i * gifDecoder.getFrameSize().width, 0, null);
			}
		}

		// Only add this directory if there is actually something to add. This will be 0
		// length for spells and npcs
		if (directory.length() > 0)
			spriteSheetContents.add("\t\t\t</dir>\n");
		spriteSheetContents.add("\t\t</dir>\n");
		spriteSheetContents.add("\t</definitions>\n");
		spriteSheetContents.add("</img>\n");

		g.dispose();

		// Prepend the output path to the front of the image name
		imageName = (outputPath != null ? outputPath + "/" : "") + imageName;
		Path path = Paths.get(imageName.replace(".gif", ".sprites"));
		File outputfile = new File(imageName.replace(".gif", ".png"));
		Files.write(path, spriteSheetContents, StandardCharsets.UTF_8);
		ImageIO.write(bim, "png", outputfile);
	}

	public static Image transformColorToTransparency(final BufferedImage im, final Color color) {
		final ImageFilter filter = new RGBImageFilter() {
			// the color we are looking for (white)... Alpha bits are set to opaque
			public int markerRGB = color.getRGB(); // | 0xFFFFFFFF;

			@Override
			public final int filterRGB(final int x, final int y, final int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) {
					// Mark the alpha bits as zero - transparent
					return 0x00FFFFFF & rgb;
				} else {
					// nothing to do
					return rgb;
				}
			}
		};

		final ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

}
