MACHINE lift

VARIABLES floor

CONSTANTS min_floor, max_floor

PROPERTIES min_floor = -2 & max_floor = 5

INITIALISATION floor := 0

INVARIANT floor : min_floor..max_floor

OPERATIONS
    floor_down =
        PRE floor > min_floor THEN
            floor := floor - 1
        END
END
