package bob.e2e.service

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.imageio.ImageIO

@Service
class KeypadService {
    private val numbers = (0..9).toList()
    private val buttonPositions = listOf(
        Pair(0, 0), Pair(50, 0), Pair(100, 0),
        Pair(0, 50), Pair(50, 50), Pair(100, 50),
        Pair(0, 100), Pair(50, 100), Pair(100, 100),
        Pair(0, 150), Pair(50, 150), Pair(100, 150)
    )

    fun generateKeypadImage(): String {
        val shuffledButtons = (numbers + listOf("blank", "blank")).shuffled()
        val keypadImage = createKeypadImage(shuffledButtons)
        val base64Image = convertToBase64(keypadImage)
        return base64Image
    }

    private fun createKeypadImage(shuffledButtons: List<Any>): BufferedImage {
        val buttonWidth = 50
        val buttonHeight = 50
        val keypadWidth = buttonWidth * 3
        val keypadHeight = buttonHeight * 4

        val keypadImage = BufferedImage(keypadWidth, keypadHeight, BufferedImage.TYPE_INT_ARGB)
        val g2d = keypadImage.createGraphics()
        g2d.color = Color.WHITE
        g2d.fillRect(0, 0, keypadWidth, keypadHeight)

        shuffledButtons.forEachIndexed { index, button ->
            val buttonImage = when (button) {
                "blank" -> loadImage("keypad/_blank.png")
                else -> loadImage("keypad/_$button.png")
            }
            val (x, y) = buttonPositions[index]
            g2d.drawImage(buttonImage, x, y, null)
        }
        g2d.dispose()

        return keypadImage
    }

    private fun loadImage(path: String): BufferedImage {
        val resource = ClassPathResource(path)
        return ImageIO.read(resource.inputStream) ?: throw IllegalStateException("Failed to load image: $path")
    }

    private fun convertToBase64(image: BufferedImage): String {
        val outputStream = ByteArrayOutputStream()
        if (!ImageIO.write(image, "png", outputStream)) {
            throw IllegalStateException("Failed to write image as PNG")
        }
        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }
}