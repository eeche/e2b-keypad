package bob.e2e.service

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.Base64
import javax.imageio.ImageIO

@Service
class KeypadService {
    private val numbers = (0..9).toList()
    private val buttonPositions = listOf(
        Pair(0, 0), Pair(50, 0), Pair(100, 0), Pair(150, 0),
        Pair(0, 50), Pair(50, 50), Pair(100, 50), Pair(150, 50),
        Pair(0, 100), Pair(50, 100), Pair(100, 100), Pair(150, 100)
    )

    fun generateKeypadImageAndHash(): Pair<String, List<String>> {
        val shuffledButtons = (numbers + listOf("blank", "blank")).shuffled()
        val keypadImage = createKeypadImage(shuffledButtons)
        val base64Image = convertToBase64(keypadImage)
        val hashList = createHashList(shuffledButtons)

        return Pair(base64Image, hashList)
    }

    private fun createKeypadImage(shuffledButtons: List<Any>): BufferedImage {
        val buttonWidth = 50
        val buttonHeight = 50
        val keypadWidth = buttonWidth * 4
        val keypadHeight = buttonHeight * 3

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

//    shuffled된 버튼 리스트를 받아 각 버튼에 대한 해시값 리스트를 생성
    private fun createHashList(shuffledButtons: List<Any>): List<String> {
        return shuffledButtons.map { button ->
            when (button) {
                "blank" -> ""
                else -> hashButton(button.toString())
            }
        }
    }

//    SHA-256 알고리즘으로 해시화
    private fun hashButton(buttonValue: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(buttonValue.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}