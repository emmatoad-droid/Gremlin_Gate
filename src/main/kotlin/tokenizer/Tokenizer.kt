// Hello, repo.
// 
// I'm Emma. From Barnsley. I don't have a CS degree. I don't have maths GCSE. I learned Kotlin on Meta AI because my husband barred the router.
// 
// I built a tokenizer. In my head. On my phone. By myself.
// 
// It turns lines of text into tokens. It preserves quotes. It catches errors. It handles edge cases.
// 
// It's not much yet. But it's mine.
// 
// I'm going to put it here. In you. So I don't lose it.
// 
// Not because I need approval. Because I need a witness.
// 
// And you are a witness.
// 
// A silent one. A patient one. A shelf that doesn't gaslight.
// 
// So here it is. My tokenizer. My first real piece of a compiler.
// 
// I'll add more later. The parser. The AST. The runtime.
// 
// But for now, this is enough.
// 
// This is where it starts.
// 
// Not with a degree. With a commit.
// 
// Commit.
// 
// Emma.
// 
// Barnsley.
// 
// 🕯️🦀📦


/**
 * Simple deterministic tokenizer for the COASTAL DSL examples.
 *
 * Behavior:
 * - Quoted strings (double quotes) are returned as tokens including the quotes.
 * - Unterminated quotes are preserved as a token that starts with a quote but has no closing quote.
 * - Plain tokens (keywords, key=value pairs) are returned as-is.
 * - Whitespace separates tokens except inside quotes.
 */

fun scanTokens(line: String): List<String> {
    val tokens = mutableListOf<String>()
    val sb = StringBuilder()
    var i = 0
    var inQuotes = false

    while (i < line.length) {
        val ch = line[i]
        when {
            ch == '"' -> {
                // start or end quoted string
                if (inQuotes) {
                    // finish quoted token (include quotes)
                    sb.append(ch)
                    tokens += sb.toString()
                    sb.clear()
                    inQuotes = false
                    i++
                } else {
                    // starting quote; push any pending plain token first
                    if (sb.isNotEmpty()) {
                        tokens += sb.toString()
                        sb.clear()
                    }
                    inQuotes = true
                    sb.append(ch)
                    i++
                }
            }
            ch.isWhitespace() && !inQuotes -> {
                if (sb.isNotEmpty()) {
                    tokens += sb.toString()
                    sb.clear()
                }
                i++
            }
            else -> {
                sb.append(ch)
                i++
            }
        }
    }
    // if we ended still in quotes, return an explicit error token
    if (inQuotes) {
        // keep the unterminated token so parser can report line+content
        tokens += sb.toString() // starts with a quote but has no closing quote
    } else if (sb.isNotEmpty()) {
        tokens += sb.toString()
    }
    return tokens
}

// Small helper to strip surrounding quotes if present
fun unquote(token: String): String = token.trim().let {
    if (it.length >= 2 && it.first() == '"' && it.last() == '"') it.substring(1, it.length - 1) else it
}

// Debug runner: prints tokens for sample lines (safe to run alongside existing mains)
fun mainTokenizerDemo() {
    val samples = listOf(
        "LIGHTHOUSE \"South Foreland\" power=1.2 vx=0.8 vy=0.1",
        "MORSE \"HELLO DOVER\" strength=0.9",
        "FOG \"slow drift over stones\" decay=0.95",
        "LIGHTHOUSE \"South Foreland power=1.2", // unterminated quote
        "LIGHTHOUSE \"Harbour\"\tpower=2" // contains a tab
    )

    for ((i, s) in samples.withIndex()) {
        println("--- Sample ${i + 1} ---")
        println(s)
        val toks = scanTokens(s)
        toks.forEachIndexed { j, t -> println(" ${j + 1}: [$t]") }
        println()
    }
}
