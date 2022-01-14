MACHINE lift

VARIABLES level

CONSTANTS min_level, max_level

PROPERTIES min_level = -2 & max_level = 5

INITIALISATION level := 0

INVARIANT level : min_level..max_level

OPERATIONS
    level_down =
        PRE level > min_level THEN
            level := level - 1
        END
END
