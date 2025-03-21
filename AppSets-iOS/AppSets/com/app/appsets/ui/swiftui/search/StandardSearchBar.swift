//
//  StandardSearchBar.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/16.
//

import SwiftUI

struct StandardSearchBar: View {
    var brokerTest: BrokerTest = BrokerTest.Instance

    let onClickListener: (String) -> Void

    init(onClick: @escaping (String) -> Void) {
        onClickListener = onClick
    }

    var body: some View {
        VStack {
            HStack {
                Button(
                    action: {
                        onClickListener("SearchBarIcon")
                    }
                ) {
                    HStack {
                        SwiftUI.Image(.Drawable.searchSearchSymbol)
                            .resizable()
                            .scaledToFit()
                            .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                            .fontWeight(.light)
                            .tint(Theme.colorSchema.onSurface)

                        Text("seach").font(.system(size: 12)).foregroundColor(Theme.colorSchema.onSurface)
                    }
                    .frame(minWidth: 70, maxWidth: 120, alignment: .leading)
                }
                .padding(12)
                .background(Theme.colorSchema.outline.clipShape(RoundedRectangle(cornerRadius: 24)))

                if LocalAccountManager.Instance.isLogged() {
                    Button(
                        action: {
                            onClickListener("UserIcon")
                        }
                    ) {
                        let borderColor = if brokerTest.isOnline {
                            Color.green
                        } else {
                            Color.red
                        }
                        AsyncImage(
                            url: URL(string: LocalAccountManager.Instance.userInfo.avatarUrl ?? ""),
                            content: { image in
                                image
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: (Theme.size.iconSizeNormal * 2) - 2, height: (Theme.size.iconSizeNormal * 2) - 2)
                                    .clipShape(Circle())
                            },
                            placeholder: {
                                SwiftUI.Image(.Drawable.faceFaceSymbol)
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                                    .padding(11)
                                    .fontWeight(.light)
                                    .tint(Theme.colorSchema.onSurface)
                            }
                        ).overlay {
                            Circle().stroke(borderColor, lineWidth: 2)
                        }
                    }

                } else {
                    Button(
                        action: {
                            onClickListener("UserIcon")
                        }
                    ) {
                        SwiftUI.Image(.Drawable.faceFaceSymbol)
                            .resizable()
                            .scaledToFit()
                            .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                            .fontWeight(.light)
                            .tint(Theme.colorSchema.onSurface)
                    }
                    .padding(12)
                    .background(Theme.colorSchema.outline.clipShape(Circle()))
                }
            }
            Spacer().frame(height: 6)
        }
    }
}

#Preview {
    StandardSearchBar(onClick: { _ in })
}
