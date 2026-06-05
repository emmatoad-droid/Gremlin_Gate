import compiler.compiler.CoastalCompiler
import compiler.compiler.CompilationResult
import runtime.CoastalRuntime

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
        ${AnsiColors.CYAN}║${AnsiColors.RESET}  ${AnsiColors.BRIGHT_BLUE}Tokenizer → Parser → AST → Compiler → SimulationPlan${AnsiColors.RESET}   ${AnsiColors.CYAN}║${AnsiColors.RESET}
        ${AnsiColors.CYAN}╚═══════════════════════════════════════════════════════════╝${AnsiColors.RESET}
    """.trimIndent()
    
    val separator = "${AnsiColors.CYAN}─────────────────────────────────────────────────────────${AnsiColors.RESET}"
    
    val success = "${AnsiColors.GREEN}✓${AnsiColors.RESET}"
    val error = "${AnsiColors.RED}✗${AnsiColors.RESET}"
    val info = "${AnsiColors.BLUE}ℹ${AnsiColors.RESET}"
    val beam = "${AnsiColors.YELLOW}◆${AnsiColors.RESET}"
    val morse = "${AnsiColors.MAGENTA}◊${AnsiColors.RESET}"
    val fog = "${AnsiColors.CYAN}≈${AnsiColors.RESET}"
}

fun main() {
    println(AsciiArt.header)
    println()

    // Example 1: Simple single statement
    executionBlock(
        title = "LIGHTHOUSE EMISSION",
        source = """
            LIGHTHOUSE "South Foreland" power=1.2 vx=0.8 vy=0.1
        """.trimIndent(),
        frames = 2
    )

    println()

    // Example 2: Multiple statements
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

    // Example 3: Default parameters
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
    
    // Source code
    println("${AnsiColors.BLUE}│${AnsiColors.RESET}  ${AnsiColors.DIM}SOURCE:${AnsiColors.RESET}")
    source.lines().forEach { line ->
        println("${AnsiColors.BLUE}│${AnsiColors.RESET}    ${AnsiColors.BRIGHT_BLACK}$line${AnsiColors.RESET}")
    }
    println("${AnsiColors.BLUE}│${AnsiColors.RESET}")

    val result = CoastalCompiler.compile(source)
    
    when (result) {
        is CompilationResult.Success -> {
            val plan = result.plan
            
            // Compilation result
            println("${AnsiColors.BLUE}│${AnsiColors.RESET}  ${AnsiColors.GREEN}COMPILED:${AnsiColors.RESET}")
            if (plan.lighthouse.isNotEmpty()) {
                println("${AnsiColors.BLUE}│${AnsiColors.RESET}    ${AsciiArt.beam} ${AnsiColors.YELLOW}Lighthouse${AnsiColors.RESET}   ${plan.lighthouse.size} emission(s)")
                plan.lighthouse.forEach { lh ->
                    println("${AnsiColors.BLUE}│${AnsiColors.RESET}       ${AnsiColors.BRIGHT_BLACK}▪ "${lh.name}" (p=${lh.power}, vx=${lh.vx}, vy=${lh.vy})${AnsiColors.RESET}")
                }
            }
            if (plan.morse.isNotEmpty()) {
                println("${AnsiColors.BLUE}│${AnsiColors.RESET}    ${AsciiArt.morse} ${AnsiColors.MAGENTA}Morse${AnsiColors.RESET}       ${plan.morse.size} signal(s)")
                plan.morse.forEach { m ->
                    println("${AnsiColors.BLUE}│${AnsiColors.RESET}       ${AnsiColors.BRIGHT_BLACK}▪ "${m.message}" (strength=${m.strength})${AnsiColors.RESET}")
                }
            }
            if (plan.fog.isNotEmpty()) {
                println("${AnsiColors.BLUE}│${AnsiColors.RESET}    ${AsciiArt.fog} ${AnsiColors.CYAN}Fog${AnsiColors.RESET}         ${plan.fog.size} field(s)")
                plan.fog.forEach { f ->
                    println("${AnsiColors.BLUE}│${AnsiColors.RESET}       ${AnsiColors.BRIGHT_BLACK}▪ "${f.phrase}" (decay=${f.decay})${AnsiColors.RESET}")
                }
            }
            println("${AnsiColors.BLUE}│${AnsiColors.RESET}")
            
            // Execution
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
            println("${AnsiColors.BLUE}│${AnsiColors.RESET}  ${AnsiColors.RED}COMPILATION FAILED:${AnsiColors.RESET}")
            println("${AnsiColors.BLUE}│${AnsiColors.RESET}    ${result.message}")
        }
    }
    
    println("${AnsiColors.BLUE}└─${AnsiColors.RESET}")
}
