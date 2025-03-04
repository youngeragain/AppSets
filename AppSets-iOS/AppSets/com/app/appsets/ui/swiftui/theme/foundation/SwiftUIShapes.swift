//
//  SwiftUIShape.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/4.
//

struct SwiftUIShapes {
    let extraSmall: SwiftUICornerBasedShape
    let small: SwiftUICornerBasedShape
    let medium: SwiftUICornerBasedShape
    let large: SwiftUICornerBasedShape
    let extraLarge: SwiftUICornerBasedShape
    
    init(
        extraSmall: SwiftUICornerBasedShape = SwiftUICornerBasedShape(),
        small: SwiftUICornerBasedShape = SwiftUICornerBasedShape(),
        medium: SwiftUICornerBasedShape = SwiftUICornerBasedShape(),
        large: SwiftUICornerBasedShape = SwiftUICornerBasedShape(),
        extraLarge: SwiftUICornerBasedShape = SwiftUICornerBasedShape()
    ) {
        self.extraSmall = extraSmall
        self.small = small
        self.medium = medium
        self.large = large
        self.extraLarge = extraLarge
    }
}
