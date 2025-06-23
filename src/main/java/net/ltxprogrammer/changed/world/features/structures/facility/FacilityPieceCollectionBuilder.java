package net.ltxprogrammer.changed.world.features.structures.facility;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.random.WeightedEntry;

public class FacilityPieceCollectionBuilder {
    private final ImmutableList.Builder<WeightedEntry.Wrapper<FacilityPiece>> builder = ImmutableList.builder();

    public static final int DEFAULT_WEIGHT = 10;

    public FacilityPieceCollectionBuilder register(FacilityPiece piece) {
        this.register(DEFAULT_WEIGHT, piece);
        return this;
    }

    public FacilityPieceCollectionBuilder register(int weight, FacilityPiece piece) {
        builder.add(WeightedEntry.wrap(piece, weight));
        return this;
    }

    public FacilityPieceCollection build() {
        return new FacilityPieceCollection(builder);
    }
}
