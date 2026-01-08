package com.rogue.domain.input;

import java.io.IOException;

public interface InputProvider {
    InputCommand readInput() throws IOException;
}
