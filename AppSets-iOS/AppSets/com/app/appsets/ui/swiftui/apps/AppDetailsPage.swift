//
//  AppDetailsPage.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import SwiftUI

struct AppDetailsPage: View {
    
    @EnvironmentObject var viewModel: MainViewModel
    
    let application: Application?
    
    let onBackClickListener: ()-> Void
    
    let onShowApplicationCreatorClickLister: (String?) -> Void
    
    let onJoinToChatClickLister: (Application) -> Void
    
    @State var currentPlatformIndex = 0
    @State var currentPlatformVersionIndex = 0
    
    init(application: Application?, onBackClick: @escaping () -> Void, onShowApplicationCreatorClick: @escaping (String?)->Void, onJoinToChatClick: @escaping (Application) -> Void) {
        self.application = application
        self.onBackClickListener = onBackClick
        self.onShowApplicationCreatorClickLister = onShowApplicationCreatorClick
        self.onJoinToChatClickLister = onJoinToChatClick
    }
    
    var body: some View {
        VStack{
            VStack(spacing:12){
                Spacer().frame(height: 68)
                BackActionTopBar(
                    backText: application?.name ?? "",
                    onBackClick: onBackClickListener,
                    customEndView: {
                        Button(
                            action: {
                                if let app = application {
                                    onJoinToChatClickLister(app)
                                }
                                
                            },
                            label: {
                                Text("chat")
                                    .tint(.white)
                                    .padding(.init(top: 8, leading: 12, bottom: 8, trailing: 12))
                                    .background(
                                        RoundedRectangle(cornerRadius: 16)
                                            .foregroundColor(.blue)
                                    )
                            })
                    }
                )
                VStack(spacing: 16){

                    AsyncImage(
                        url: URL(string: application?.iconUrl ?? ""),
                        content: { image in
                            image
                                .resizable()
                                .scaledToFit()
                                .frame(width: 250)
                                .clipShape(RoundedRectangle(cornerRadius: 32))
                        },
                        placeholder: {
                            RoundedRectangle(cornerRadius: 32)
                                .frame(width: 250, height: 250, alignment: .center)
                                .foregroundColor(Color(UIColor.separator))
                        }
                    )
                
                }
                if let downloadInfoList = application?.platforms?[currentPlatformIndex].versionInfos?[currentPlatformVersionIndex].downloadInfo {
                    if(!downloadInfoList.isEmpty){
                        Button(
                            action: {
                                
                            },
                            label: {
                                Text("get")
                                    .tint(.white)
                                    .padding(.init(top: 8, leading: 12, bottom: 8, trailing: 12))
                                    .background(
                                        RoundedRectangle(cornerRadius: 16)
                                            .foregroundColor(.blue)
                                    )
                            })
                    }
                }
                
                VStack{
                    HStack{
                        Spacer()
                        Text("Creator Information")
                        Spacer()
                    }
                    .padding()
                    .background(RoundedRectangle(cornerRadius: 12).foregroundColor(Color(uiColor: .separator)))
                }.padding()
                VStack{
                    VStack(spacing: 6){
                        HStack{
                            VStack(alignment: .leading, spacing: 4){
                                Text("Category").font(.system(size:14, weight:.bold))
                                Text(application?.category ?? "").font(.system(size: 12))
                            }
                            Spacer()
                        }
                        HStack{
                            VStack(alignment: .leading, spacing: 4){
                                Text("Website").font(.system(size:14, weight:.bold))
                                Text(application?.website ?? "").font(.system(size: 12))
                            }
                            Spacer()
                        }
                        HStack{
                            VStack(alignment: .leading, spacing: 4){
                                Text("Developer Information").font(.system(size:14, weight:.bold))
                                Text(application?.developerInfo ?? "").font(.system(size: 12))
                            }
                            Spacer()
                        }
                    }
                    .padding()
                    .overlay(
                        content: {
                            RoundedRectangle(cornerRadius: 16, style: .continuous)
                                .stroke()
                                .foregroundColor(Color(uiColor: .separator))
                                .cornerRadius(16)
                        }
                    )
                }.padding()
                Spacer()
            }
        }.frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
            .edgesIgnoringSafeArea(.all)
    }
}

#Preview {
    AppDetailsPage(
        application: Application(),
        onBackClick: {
        
        },
        onShowApplicationCreatorClick: { uid in
            
        },
        onJoinToChatClick: { application in
            
        }
    )
}
