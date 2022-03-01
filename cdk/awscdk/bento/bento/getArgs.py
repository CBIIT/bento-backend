import sys, getopt

class getArgs:
  def set_tier(argv):
    
    try:
      opts, args = getopt.getopt(argv,"ht:",["tier="])
    except getopt.GetoptError:
      print('awsApp.py -t <tier>')
      sys.exit(2)
    for opt, arg in opts:
      if opt == '-h':
         print('To use these scripts please identify the tier:  awsApp.py -t <tier>')
         sys.exit(1)
      elif opt in ("-t", "--tier"):
        return arg