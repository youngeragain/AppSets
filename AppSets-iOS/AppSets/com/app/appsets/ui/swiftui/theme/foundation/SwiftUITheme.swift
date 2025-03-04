//
//  SwiftUITheme.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/4.
//

struct SwiftUITheme {
    let colorSchema: ColorScheme
    let shapes: SwiftUIShapes
    let typography: SwiftUITypography
    let size: SwiftUISize

    init(colorSchema: ColorScheme, shapes: SwiftUIShapes, typography: SwiftUITypography, size: SwiftUISize) {
        self.colorSchema = colorSchema
        self.shapes = shapes
        self.typography = typography
        self.size = size
    }
}
