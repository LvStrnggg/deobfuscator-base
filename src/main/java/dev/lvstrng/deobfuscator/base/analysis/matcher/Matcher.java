package dev.lvstrng.deobfuscator.base.analysis.matcher;

import dev.lvstrng.deobfuscator.base.analysis.MethodContext;
import dev.lvstrng.deobfuscator.base.analysis.matcher.matches.Match;

import java.util.ArrayList;
import java.util.List;

public class Matcher {
    private final Match[] matches;

    private Matcher(Match... matches) {
        this.matches = matches;
    }

    public List<MatchContext> findAllMatches(MethodContext context) {
        var list = new ArrayList<MatchContext>();

        var first = matches[0];
        for(var insn : context.method().instructions) {
            if(!first.test(insn))
                continue;

            int idx = context.method().instructions.indexOf(insn);
            if(idx + matches.length > context.method().instructions.size())
                continue;

            var match = new MatchContext(context);
            var complete = true;

            for(int i = 0; i < matches.length; i++) {
                var curr = context.method().instructions.get(idx + i);
                var currMatch = matches[i];
                var passed = currMatch.test(curr);

                if(!passed) {
                    complete = false;
                    break;
                }

                match.add(curr);
                if(currMatch.capture() == null)
                    continue;

                match.put(currMatch.capture(), curr);
            }

            if(!complete)
                continue;

            list.add(match);
        }

        return list;
    }

    public static Matcher of(Match... matches) {
        return new Matcher(matches);
    }
}
