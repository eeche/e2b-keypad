package bob.e2e.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.imageio.ImageIO
import org.slf4j.LoggerFactory
// 이 컨트롤러의 기본 경로
@RequestMapping("/keypad")
// 이 클래스가 RESTful 웹 서비스 컨트롤러
@RestController
class KeypadController {
    // 로깅을 위한 logger 객체 생성
    private val logger = LoggerFactory.getLogger(KeypadController::class.java)
    // 0~9까지 숫자 리스트 생성
    private val numbers = (0..9).toList()
    // 버튼의 위치 지정 총 12개
    private val buttonPositions = listOf(
        Pair(0, 0), Pair(50, 0), Pair(100, 0),
        Pair(0, 50), Pair(50, 50), Pair(100, 50),
        Pair(0, 100), Pair(50, 100), Pair(100, 100),
        Pair(0, 150), Pair(50, 150), Pair(100, 150)
    )
    // 숫자+빈칸 무작위로 셔플하는 함수
    @GetMapping
    fun getKeypad(): ResponseEntity<Map<String, String>> {
        val shuffledButtons = (numbers + listOf("blank", "blank")).shuffled()

        // 첫 번째 버튼 이미지를 로드하여 크기 얻어옴
        val firstButtonImage = loadImage("keypad/_0.png")
        val buttonWidth = firstButtonImage.width
        val buttonHeight = firstButtonImage.height

        // 전체 키패드 이미지 크기 계산
        val keypadWidth = buttonWidth * 3
        val keypadHeight = buttonHeight * 4

        // 새로운 빈 이미지 생성 (배경색을 흰색으로 설정)
        val keypadImage = BufferedImage(keypadWidth, keypadHeight, BufferedImage.TYPE_INT_ARGB)
        val g2d = keypadImage.createGraphics()
        g2d.color = Color.WHITE
        g2d.fillRect(0, 0, keypadWidth, keypadHeight)
        //
        shuffledButtons.forEachIndexed { index, button ->
            val buttonImage = when (button) {
                "blank" -> loadImage("keypad/_blank.png")
                else -> loadImage("keypad/_$button.png")
            }
//            logger.info("Button $button image loaded: ${buttonImage.width}x${buttonImage.height}")
            val (x, y) = buttonPositions[index]
            logger.info("Button $button loaded: ${buttonImage.width}x${buttonImage.height}, position: ($x, $y)")
            g2d.drawImage(buttonImage, x, y, null)
        }
        g2d.dispose()

//        logger.info("Final keypad image size: ${keypadImage.width}x${keypadImage.height}")

        val base64Image = convertToBase64(keypadImage)
        logger.info("Base64 image length: ${base64Image.length}")

        val response = mapOf("keypadImage" to base64Image)
        return ResponseEntity.ok(response)
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