import kotlin.math.floorMod

// ============================================================================
// 🌊 COASTAL DSL COMPILER & RUNTIME MODULES
// ============================================================================

sealed interface CompilationResult {
    data class Success(val plan: SimulationPlan) : CompilationResult
    data class Error(val message: String, val line: Int = 0) : CompilationResult
}

data class SimulationPlan(
    val lighthouse: List<LighthouseDef> = emptyList(),
    val morse: List<MorseDef> = emptyList(),
    val fog: List<FogDef> = emptyList()
)

data class LighthouseDef(val name: String, val power: Double, val vx: Double, val vy: Double)
data class MorseDef(val message: String, val strength: Double)
data class FogDef(val phrase: String, val decay: Double)

object CoastalCompiler {

    fun compile(source: String): CompilationResult {
        val lighthouses = mutableListOf<LighthouseDef>()
        val morses = mutableListOf<MorseDef>()
        val fogs = mutableListOf<FogDef>()

        source.lines().forEachIndexed { index, rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("//")) return@forEachIndexed
            
            try {
                val tokens = tokenize(line)
                if (tokens.isEmpty()) return@forEachIndexed
                
                when (val command = tokens[0].uppercase()) {
                    "LIGHTHOUSE" -> lighthouses += parseLighthouse(tokens)
                    "MORSE" -> morses += parseMorse(tokens)
                    "FOG" -> fogs += parseFog(tokens)
                    else -> return CompilationResult.Error("Unknown symbol '$command'", index + 1)
                }
            } catch (e: Exception) {
                return CompilationResult.Error("Parse failure: ${e.message}", index + 1)
            }
        }

        return CompilationResult.Success(SimulationPlan(lighthouses, morses, fogs))
    }

    private fun tokenize(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var sb = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    inQuotes = !inQuotes
                    sb.append(ch)
                    i++
                }
                ch == ' ' && !inQuotes -> {
                    if (sb.isNotEmpty()) {
                        tokens += sb.toString()
                        sb = StringBuilder()
                    }
                    i++
                }
                else -> {
                    sb.append(ch)
                    i++
                }
            }
        }
        if (sb.isNotEmpty()) tokens += sb.toString()
        return tokens
    }

    private fun extractQuote(token: String): String {
        val quoted = token.trim()
        return if (quoted.startsWith("\"") && quoted.endsWith("\"")) {
            quoted.substring(1, quoted.length - 1)
        } else {
            quoted
        }
    }

    private fun parseProperties(tokens: List<String>): Map<String, String> {
        val props = mutableMapOf<String, String>()
        tokens.drop(2).forEach { token ->
            val parts = token.split("=")
            if (parts.size == 2) {
                props[parts[0].lowercase()] = parts[1]
            }
        }
        return props
    }

    private fun parseLighthouse(tokens: List<String>): LighthouseDef {
        if (tokens.size < 2) throw IllegalArgumentException("LIGHTHOUSE requires name string")
        val name = extractQuote(tokens[1])
        val props = parseProperties(tokens)
        
        return LighthouseDef(
            name = name,
            power = props["power"]?.toDoubleOrNull() ?: 1.0,
            vx = props["vx"]?.toDoubleOrNull() ?: 1.0,
            vy = props["vy"]?.toDoubleOrNull() ?: 0.2
        )
    }

    private fun parseMorse(tokens: List<String>): MorseDef {
        if (tokens.size < 2) throw IllegalArgumentException("MORSE requires message string")
        val message = extractQuote(tokens[1])
        val props = parseProperties(tokens)
        
        return MorseDef(
            message = message,
            strength = props["strength"]?.toDoubleOrNull() ?: 1.0
        )
    }

    private fun parseFog(tokens: List<String>): FogDef {
        if (tokens.size < 2) throw IllegalArgumentException("FOG requires phrase string")
        val phrase = extractQuote(tokens[1])
        val props = parseProperties(tokens)
        
        return FogDef(
            phrase = phrase,
            decay = props["decay"]?.toDoubleOrNull() ?: 0.97
        )
    }
}

class CoastalRuntime(val plan: SimulationPlan) {
    private val log = mutableListOf<String>()

    fun run(frames: Long) {
        for (f in 1..frames) {
            plan.lighthouse.forEach { lh ->
                log += "[FRAME $f] LIGHTHOUSE '${lh.name}' sweeping beam (power=${lh.power}) with delta velocity [${lh.vx}, ${lh.vy}]"
            }
            plan.morse.forEach { m ->
                log += "[FRAME $f] MORSE modulation pulsing sequence '${m.message}' at amplitude ${m.strength}"
            }
            plan.fog.forEach { fFields ->
                log += "[FRAME $f] FOG atmosphere bank '${fFields.phrase}' blanketing canvas (dissipation constant=${fFields.decay})"
            }
        }
    }

    fun getLog(): List<String> = log
}

// ============================================================================
// 🎨 ANSI INTERFACE DISPLAY & PROGRAM ENTRYPOINT
// ============================================================================

object AnsiColors {
    const val RESET = "\u001B[0m"
    const val BOLD = "\u001B[1m"
    const val DIM = "\u001B[2m"
    const val ITALIC = "\u001B[3m"
    const val UNDERLINE = "\u001B[4m"
    
    const val BLACK = "\u001B[30m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val MAGENTA = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"
    
    const val BG_BLACK = "\u001B[40m"
    const val BG_RED = "\u001B[41m"
    const val BG_GREEN = "\u001B[42m"
    const val BG_YELLOW = "\u001B[43m"
    const val BG_BLUE = "\u001B[44m"
    const val BG_MAGENTA = "\u001B[45m"
    const val BG_CYAN = "\u001B[46m"
    const val BG_WHITE = "\u001B[47m"
    
    const val BRIGHT_BLACK = "\u001B[90m"
    const val BRIGHT_RED = "\u001B[91m"
    const val BRIGHT_GREEN = "\u001B[92m"
    const val BRIGHT_YELLOW = "\u001B[93m"
    const val BRIGHT_BLUE = "\u001B[94m"
    const val BRIGHT_MAGENTA = "\u001B[95m"
    const val BRIGHT_CYAN = "\u001B[96m"
    const val BRIGHT_WHITE = "\u001B[97m"
}

object AsciiArt {
    val header = """
        ${AnsiColors.CYAN}╔═══════════════════════════════════════════════════════════╗${AnsiColors.RESET}
        ${AnsiColors.CYAN}║${AnsiColors.RESET}    ${AnsiColors.BOLD}🌊 COASTAL DSL COMPILER${AnsiColors.RESET}  ${AnsiColors.BRIGHT_CYAN}v0.1.0${AnsiColors.RESET}            ${AnsiColors.CYAN}║${AnsiColors.RESET}
        ${AnsiColors.CYAN}╠═══════════════════════════════════════════════════════════╣${AnsiColors.RESET}
        ${AnsiColors.CYAN}║${AnsiColors.RESET}  ${AnsiColors.BRIGHT_BLUE}Tokenizer → Parser → Compiler → SimulationPlan → Runtime${AnsiColors.RESET}  ${AnsiColors.CYAN}║${AnsiColors.RESET}
        ${AnsiColors.CYAN}╚═══════════════════════════════════════════════════════════╝${AnsiColors.RESET}
    """.trimIndent()
    
    val success = "${AnsiColors.GREEN}✓${AnsiColors.RESET}"
    val error = "${AnsiColors.RED}✗${AnsiColors.RESET}"
    val beam = "${AnsiColors.YELLOW}◆${AnsiColors.RESET}"
    val morse = "${AnsiColors.MAGENTA}◊${AnsiColors.RESET}"
    val fog = "${AnsiColors.CYAN}≈${AnsiColors.RESET}"
}

fun main() {
    println(AsciiArt.header)
    println()

    executionBlock(
        title = "LIGHTHOUSE EMISSION",
        source = """
            LIGHTHOUSE "South Foreland" power=1.2 vx=0.8 vy=0.1
        """.trimIndent(),
        frames = 2
    )

    println()

    executionBlock(
        title = "COASTAL SCENE",
        source = """
            LIGHTHOUSE "South Foreland" power=1.4 vx=0.2 vy=0.0
            MORSE "HELLO DOVER" strength=0.9
            FOG "sea memory drifting inland" decay=0.96
        """.trimIndent(),
        frames = 3
    )

    println()

    executionBlock(
        title = "MINIMAL CONFIGURATION",
        source = """
            LIGHTHOUSE "Simple"
            MORSE "MSG"
            FOG "Fog"
        """.trimIndent(),
        frames = 2
    )

    println()
    println(AsciiArt.header)
}

private fun executionBlock(title: String, source: String, frames: Int) {
    println("${AnsiColors.BOLD}${AnsiColors.BLUE}┌─ $title${AnsiColors.RESET}")
    println("${AnsiColors.BLUE}│${AnsiColors.RESET}")
    
    println("${AnsiColors.BLUE}│${AnsiColors.RESET}  ${AnsiColors.DIM}SOURCE:${AnsiColors.RESET}")
    source.lines().forEach { line ->
        println("${AnsiColors.BLUE}│${AnsiColors.RESET}    ${AnsiColors.BRIGHT_BLACK}$line${AnsiColors.RESET}")
    }
    println("${AnsiColors.BLUE}│${AnsiColors.RESET}")

    val result = CoastalCompiler.compile(source)
    
    when (result) {
        is CompilationResult.Success -> {
            val plan = result.plan
            
            println("${AnsiColors.BLUE}│${AnsiColors.RESET}  ${AnsiColors.GREEN}${AsciiArt.success} COMPILED:${AnsiColors.RESET}")
            if (plan.lighthouse.isNotEmpty()) {
                println("${AnsiColors.BLUE}│${AnsiColors.RESET}    ${AsciiArt.beam} ${AnsiColors.YELLOW}Lighthouse${AnsiColors.RESET}   ${plan.lighthouse.size} emission(s)")
                plan.lighthouse.forEach { lh ->
                    println("${AnsiColors.BLUE}│${AnsiColors.RESET}       ${AnsiColors.BRIGHT_BLACK}▪ '${lh.name}' (p=${lh.power}, vx=${lh.vx}, vy=${lh.vy})${AnsiColors.RESET}")
                }
            }
            if (plan.morse.isNotEmpty()) {
                println("${AnsiColors.BLUE}│${AnsiColors.RESET}    ${AsciiArt.morse} ${AnsiColors.MAGENTA}Morse${AnsiColors.RESET}       ${plan.morse.size} signal(s)")
                plan.morse.forEach { m ->
                    println("${AnsiColors.BLUE}│${AnsiColors.RESET}       ${AnsiColors.BRIGHT_BLACK}▪ '${m.message}' (strength=${m.strength})${AnsiColors.RESET}")
                }
            }
            if (plan.fog.isNotEmpty()) {
                println("${AnsiColors.BLUE}│${AnsiColors.RESET}    ${AsciiArt.fog} ${AnsiColors.CYAN}Fog${AnsiColors.RESET}         ${plan.fog.size} field(s)")
                plan.fog.forEach { f ->
                    println("${AnsiColors.BLUE}│${AnsiColors.RESET}       ${AnsiColors.BRIGHT_BLACK}▪ '${f.phrase}' (decay=${f.decay})${AnsiColors.RESET}")
                }
            }
            println("${AnsiColors.BLUE}│${AnsiColors.RESET}")
            
            val runtime = CoastalRuntime(plan)
            runtime.run(frames.toLong())
            
            println("${AnsiColors.BLUE}│${AnsiColors.RESET}  ${AnsiColors.CYAN}EXECUTION (${frames} frames):${AnsiColors.RESET}")
            runtime.getLog().forEach { log ->
                val symbol = when {
                    log.contains("LIGHTHOUSE") -> AsciiArt.beam
                    log.contains("MORSE") -> AsciiArt.morse
                    log.contains("FOG") -> AsciiArt.fog
                    else -> " "
                }
                println("${AnsiColors.BLUE}│${AnsiColors.RESET}    $symbol ${AnsiColors.BRIGHT_BLACK}$log${AnsiColors.RESET}")
            }
        }
        is CompilationResult.Error -> {
            println("${AnsiColors.BLUE}│${AnsiColors.RESET}  ${AnsiColors.RED}${AsciiArt.error} COMPILATION FAILED (Line ${result.line}):${AnsiColors.RESET}")
            println("${AnsiColors.BLUE}│${AnsiColors.RESET}    ${result.message}")
        }
    }
    
    println("${AnsiColors.BLUE}└─${AnsiColors.RESET}")
}
