import java.awt.*
import java.awt.RenderingHints.*
import java.awt.event.*
import java.time.*
import java.time.format.DateTimeFormatter
import javax.sound.sampled.*
import javax.swing.*

fun main() {
    frame(panel(alarm(), clip(), font()))
}

fun frame(panel: JPanel) = JFrame("Alarm").also {
    it.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    it.contentPane = panel
    it.isResizable = false
    it.pack()
    it.setLocationRelativeTo(null)
    it.isVisible = true
}

fun panel(alarm: Alarm, clip: Clip, font: Font) : JPanel {
    val size = Dimension(400, 200)
    val panel = object: JPanel() {
        override fun paintComponent(graphics: Graphics) {
            graphics.render(size, alarm, font)
        }
    }
    with(panel) {
        preferredSize = size
        addMouseWheelListener {
            val max = intArrayOf(24, 60)
            val target = it.x * 2 / size.width
            alarm.time[target] -= it.wheelRotation
            alarm.time[target] %= max[target]
            if (alarm.time[target] == -1) alarm.time[target] = max[target] - 1
            repaint()
        }
        addMouseListener(object: MouseAdapter() {
            override fun mousePressed(event: MouseEvent) {
                alarm.turnedOn = !alarm.turnedOn
                val closeOperation = if (alarm.turnedOn) JFrame.DO_NOTHING_ON_CLOSE else JFrame.EXIT_ON_CLOSE
                (SwingUtilities.getWindowAncestor(panel) as JFrame).defaultCloseOperation = closeOperation
                if (!alarm.turnedOn) alarm.triggered = false
                repaint()
            }
        })
    }
    Timer(200) {
        panel.repaint()
        val now = LocalTime.now()
        if (now.hour == alarm.time[0] && now.minute == alarm.time[1] && alarm.turnedOn)
            alarm.triggered = true
        if (alarm.triggered)
            clip.setFramePosition(0).also { clip.start() }
    }.start()
    return panel
}

fun Graphics.render(size: Dimension, alarm: Alarm, font: Font) {
    (this as Graphics2D).setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
    color = Color(0xFF, 0xD0, 0xD0)
    fillRect(0, 0, size.width, size.height)

    val alarmString = "%02d:%02d".format(alarm.time[0], alarm.time[1])
    color = Color.WHITE
    this.font = font.deriveFont(64f)
    val bound = fontMetrics.getStringBounds(alarmString, this)
    drawString(
        alarmString,
        (size.width - bound.width.toInt()) / 2,
        (size.height - bound.height.toInt()) / 2 + fontMetrics.ascent,
    )
    val padding = 8
    val diameter = 12

    this.font = font.deriveFont(16f)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    val now = LocalDateTime.now().format(formatter)
    drawString(now, padding, size.height - padding)

    color = if (alarm.turnedOn) Color.GREEN else Color.WHITE
    val offset = padding + diameter
    fillOval(size.width - offset, size.height - offset, diameter, diameter)
}

data class Alarm(var time: IntArray, var turnedOn: Boolean, var triggered: Boolean)
fun alarm() = Alarm(IntArray(2), false, false)
fun resource(path: String) = object {}.javaClass.getResource(path)!!
fun font() = Font.createFonts(resource("font.ttf").openStream())[0]
fun clip() = AudioSystem.getClip().also {
    it.open(AudioSystem.getAudioInputStream(resource("alarm.wav")))
}
